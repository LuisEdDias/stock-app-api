package lat.luisdias.stock_app_main_service.security.identity;

import jakarta.persistence.EntityNotFoundException;
import lat.luisdias.stock_app_main_service.security.authentication.AuthenticatedUser;
import lat.luisdias.stock_app_main_service.security.identity.dto.GetUserDTO;
import lat.luisdias.stock_app_main_service.security.identity.dto.StoreUserDTO;
import lat.luisdias.stock_app_main_service.security.identity.dto.UpdateEmailDTO;
import lat.luisdias.stock_app_main_service.security.identity.dto.UpdatePasswordDTO;
import lat.luisdias.stock_app_main_service.security.authorization.UserRole;
import lat.luisdias.stock_app_main_service.security.authentication.two.factor.auth.TwoFactorCodeEncoder;
import lat.luisdias.stock_app_main_service.infra.mailsender.MailSenderService;
import lat.luisdias.stock_app_main_service.security.authentication.two.factor.auth.TwoFactorAuthService;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final TwoFactorAuthService twoFactorAuthService;
    private final MailSenderService mailSenderService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.credential.password-expire-time}")
    private Long passwordExpireDurationDays;

    public UserService(
            UserRepository userRepository,
            TwoFactorAuthService twoFactorAuthService,
            MailSenderService mailSenderService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.twoFactorAuthService = twoFactorAuthService;
        this.mailSenderService = mailSenderService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<GetUserDTO> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(GetUserDTO::new);
    }

    @Transactional(readOnly = true)
    public GetUserDTO findByPublicId(UUID subject) {
        return userRepository.findBySubject(subject).map(GetUserDTO::new).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found")));
    }


    public GetUserDTO storeUser(StoreUserDTO storeUserDTO) {
        if (storeUserDTO.role() == UserRole.ROOT) {
            throw new IllegalArgumentException(I18n.get("validation.user_role_not_allowed"));
        }
        confirmPassword(storeUserDTO.password(), storeUserDTO.confirmPassword());
        emailAlreadyRegistered(storeUserDTO.email());
        User user = userRepository.save(
                new User(
                        UUID.randomUUID(),
                        storeUserDTO.email(),
                        passwordEncoder.encode(storeUserDTO.password()),
                        storeUserDTO.nickname(),
                        storeUserDTO.role(),
                        AccountStatus.PENDING_ACTIVATION
                )
        );

        mailSenderService.sendMail(
                user.getEmail(),
                "StockApp",
                "Sua conta foi criada com sucesso! No seu primeiro login será necessário realizar a alteração da senha."
        );

        return new GetUserDTO(user);
    }

    public String enableTwoFactorAuth(AuthenticatedUser authenticatedUser) {
        User user = findUserBySubject(authenticatedUser.subject());
        String secret = twoFactorAuthService.generateSecretKey();
        user.enableTwoFactor(TwoFactorCodeEncoder.encrypt(secret));

        String otpAuthURI = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s",
                "Stock App",
                user.getEmail(),
                secret,
                "Stock App"
        );

        mailSenderService.sendMail(
                user.getEmail(),
                "Segurança",
                "A autenticação de dois fatores foi ativada na sua conta."
        );

        return otpAuthURI;
    }

    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        confirmPassword(updatePasswordDTO.newPassword(), updatePasswordDTO.confirmPassword());
        User user = findUserBySubject(updatePasswordDTO.subject());
        confirmAuthentication(updatePasswordDTO.oldPassword(), user.getPasswordHash());
        user.changePassword(
                passwordEncoder.encode(updatePasswordDTO.newPassword()),
                Duration.ofDays(passwordExpireDurationDays)
        );
    }

    public void updateEmail(UpdateEmailDTO updateEmailDTO) {
        User user = findUserBySubject(updateEmailDTO.subject());
        throwIfRoot(user);
        confirmAuthentication(updateEmailDTO.password(), user.getPasswordHash());
        emailAlreadyRegistered(updateEmailDTO.email());
        user.changeEmail(updateEmailDTO.email());
    }

    public void deleteUser(UUID subject) {
        User user = findUserBySubject(subject);
        throwIfRoot(user);
        userRepository.delete(user);
    }

    private User findUserBySubject(UUID subject) {
        return userRepository.findBySubject(subject).orElseThrow(
                () -> new EntityNotFoundException(I18n.get("exception.not_found"))
        );
    }

    private void emailAlreadyRegistered(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(I18n.get("exception.email_already_registered"));
        }
    }

    private void confirmPassword(String password, String confirmPassword) {
        if (!(password.equals(confirmPassword))) {
            throw new IllegalArgumentException(I18n.get("validation.password.not_match"));
        }
    }

    public void confirmAuthentication(String password, String currentPassword) {
        if (!(passwordEncoder.matches(password, currentPassword))) {
            throw new IllegalArgumentException(I18n.get("exception.authentication_credentials"));
        }
    }

    public void throwIfRoot(User user) {
        if (user.getRole() == UserRole.ROOT) {
            throw new IllegalArgumentException(I18n.get("exception.root_user_immutable_throw"));
        }
    }
}
