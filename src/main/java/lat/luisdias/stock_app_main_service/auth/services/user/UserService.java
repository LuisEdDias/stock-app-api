package lat.luisdias.stock_app_main_service.auth.services.user;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.auth.dto.admin.StoreRootUserDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.GetUserDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.StoreUserDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.UpdateEmailDTO;
import lat.luisdias.stock_app_main_service.auth.dto.user.UpdatePasswordDTO;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.entities.user.UserRoles;
import lat.luisdias.stock_app_main_service.auth.infra.util.CryptoUtil;
import lat.luisdias.stock_app_main_service.auth.repositories.user.UserRepository;
import lat.luisdias.stock_app_main_service.auth.services.MailSenderService;
import lat.luisdias.stock_app_main_service.auth.services.auth.TwoFactorAuthService;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final MailSenderService mailSenderService;

    public UserService(UserRepository userRepository, TwoFactorAuthService twoFactorAuthService, MailSenderService mailSenderService) {
        this.userRepository = userRepository;
        this.twoFactorAuthService = twoFactorAuthService;
        this.mailSenderService = mailSenderService;
    }

    public Page<GetUserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(GetUserDTO::new);
    }

    public GetUserDTO findById(Long id) {
        return userRepository.findById(id).map(GetUserDTO::new).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found")));
    }

    public GetUserDTO storeUser(StoreUserDTO storeUserDTO) {
        if (storeUserDTO.role().equals(UserRoles.ROOT)) {
            throw new IllegalArgumentException(I18n.get("validation.user_role"));
        }
        confirmPassword(storeUserDTO.password(), storeUserDTO.confirmPassword());
        emailAlreadyRegistered(storeUserDTO.email());
        User user = userRepository.save(new User(storeUserDTO));
        return new GetUserDTO(user);
    }

    public HashMap<String, String> storeRootUser(StoreRootUserDTO storeRootUserDTO) {
        if (userRepository.existsByRole(UserRoles.ROOT)) {
            throw new IllegalArgumentException(I18n.get("exception.root_user_exists"));
        }
        confirmPassword(storeRootUserDTO.password(), storeRootUserDTO.confirmPassword());
        emailAlreadyRegistered(storeRootUserDTO.email());
        User user = new User(storeRootUserDTO);
        String twoFAQrcode = enableTwoFactorAuth(user);
        userRepository.save(user);
        HashMap<String, String> map = new HashMap<>();
        map.put("twoFAQrcode", twoFAQrcode);
        map.put("userId", user.getId().toString());
        return map;
    }

    public String enableTwoFactorAuth(User user) {
        String secret = twoFactorAuthService.generateSecretKey();
        user.enableTwoFAuth(CryptoUtil.encrypt(secret));
        userRepository.save(user);
        String prefix = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=";
        String qrCode = prefix + URLEncoder.encode(
                String.format(
                        "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                        "Stock App", user.getNickname(),
                        secret, "Stock App"),
                StandardCharsets.UTF_8);

        mailSenderService.sendMail(user.getUsername(), "Habilitar 2FA", qrCode);

        return qrCode;
    }

    public void updatePassword(UpdatePasswordDTO updatePasswordDTO, User user) {
        confirmPassword(updatePasswordDTO.newPassword(), updatePasswordDTO.confirmPassword());
        confirmAuthentication(updatePasswordDTO.oldPassword(), user.getPassword());
        user.updatePassword(updatePasswordDTO.newPassword());
        userRepository.save(user);
    }

    public void updateEmail(UpdateEmailDTO updateEmailDTO, User user){
        confirmAuthentication(updateEmailDTO.password(), user.getPassword());
        emailAlreadyRegistered(updateEmailDTO.email());
        user.updateEmail(updateEmailDTO.email());
    }

    public void deleteUser(Long id) {
        userRepository.findById(id)
                .ifPresentOrElse(
                        userRepository::delete,
                        () -> {throw new EntityNotFoundException(I18n.get("exception.not_found"));}
                );
    }

    private void emailAlreadyRegistered(String email) {
        if (userRepository.existsByEmail(email)){
            throw new IllegalArgumentException(I18n.get("exception.email_already_registered"));
        }
    }

    private void confirmPassword(String password, String confirmPassword){
        if (!(password.equals(confirmPassword))){
            throw new IllegalArgumentException(I18n.get("validation.password.not_match"));
        }
    }

    public void confirmAuthentication(String password, String currentPassword){
        if (!(new BCryptPasswordEncoder().matches(password, currentPassword))){
            throw new IllegalArgumentException(I18n.get("exception.authentication_credentials"));
        }
    }
}
