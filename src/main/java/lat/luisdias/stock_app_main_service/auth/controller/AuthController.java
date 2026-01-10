package lat.luisdias.stock_app_main_service.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lat.luisdias.stock_app_main_service.auth.dto.auth.LoginDTO;
import lat.luisdias.stock_app_main_service.auth.dto.auth.TokenDTO;
import lat.luisdias.stock_app_main_service.auth.dto.auth.TwoFADTO;
import lat.luisdias.stock_app_main_service.auth.entities.logs.AuthLog;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.entities.user.UserRoles;
import lat.luisdias.stock_app_main_service.auth.infra.util.CryptoUtil;
import lat.luisdias.stock_app_main_service.auth.infra.util.GeoLocationService;
import lat.luisdias.stock_app_main_service.auth.services.auth.AuthLogService;
import lat.luisdias.stock_app_main_service.auth.services.auth.AuthService;
import lat.luisdias.stock_app_main_service.auth.services.auth.TwoFactorAuthService;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/v1/login")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final AuthLogService authLogService;
    private final GeoLocationService geoLocationService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(
            AuthenticationManager authenticationManager,
            AuthService authService,
            TwoFactorAuthService twoFactorAuthService,
            AuthLogService authLogService,
            GeoLocationService geoLocationService
    ) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
        this.twoFactorAuthService = twoFactorAuthService;
        this.authLogService = authLogService;
        this.geoLocationService = geoLocationService;
    }

    @PostMapping
    public ResponseEntity<TokenDTO> login(
            @RequestBody @Valid LoginDTO loginDTO,
            HttpServletRequest request
    ) {
        String userIp = getUserIp(request);
        logger.info(
                "Login request received from ip ({}) with username ({})",
                userIp,
                maskEmail(loginDTO.email())
        );
        try {
            var authToken = new UsernamePasswordAuthenticationToken(loginDTO.email(), loginDTO.password());
            User user = (User)authenticationManager.authenticate(authToken).getPrincipal();

            if (user.isTwoFAuth() || user.getAuthorities().contains(new SimpleGrantedAuthority(UserRoles.ROOT.name()))) {
                String tokenPartial = authService.getPartialToken(user);
                logger.info(
                        "LOGIN SUCCESS from ip ({}) with username ({})",
                        userIp,
                        maskEmail(loginDTO.email())
                );
                entryLog(
                        userIp,
                        user.getId(),
                        "LOGIN with username (" + loginDTO.email() + ") PARTIAL TOKEN",
                        true
                );
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(new TokenDTO(tokenPartial));
            }
            var tokenJWT = authService.getToken(user);
            logger.info(
                    "LOGIN SUCCESS from ip ({}) with username ({})",
                    userIp,
                    maskEmail(loginDTO.email())
            );
            entryLog(
                    userIp,
                    user.getId(),
                    "LOGIN with username (" + loginDTO.email() + ")",
                    true
            );
            return ResponseEntity.ok(new TokenDTO(tokenJWT));
        } catch (AuthenticationException e) {
            logger.warn("LOGIN FAIL from ip ({}) with username ({})", userIp, maskEmail(loginDTO.email()));
            entryLog(
                    userIp,
                    null,
                    "LOGIN FAIL with username (" + loginDTO.email() + ")",
                    false
            );
            throw new BadCredentialsException(I18n.get("exception.authentication_credentials"));
        }
    }

    @PostMapping("/validate-2fa")
    public ResponseEntity<TokenDTO> validate2FA(
            @RequestBody @Valid TwoFADTO twoFADTO,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        String userIp = getUserIp(request);
        logger.info(
                "2FA validation attempt from ip ({}) with username ({})",
                userIp,
                maskEmail(user.getUsername())
        );
        try {
            String secret = CryptoUtil.decrypt(user.getTwoFASecret());
            twoFactorAuthService.isCodeValid(secret, Integer.parseInt(twoFADTO.totpCode()));
            String tokenJWT = authService.getToken(user);
            logger.info(
                    "2FA validation SUCCESS from ip ({}) with username ({})",
                    userIp,
                    maskEmail(user.getUsername())
            );
            entryLog(
                    userIp,
                    user.getId(),
                    "2FA validation with username (" + user.getUsername() + ")",
                    true
            );
            return ResponseEntity.ok(new TokenDTO(tokenJWT));
        } catch (BadCredentialsException e) {
            logger.info(
                    "2FA validation FAIL from ip ({}) with username ({})",
                    userIp,
                    maskEmail(user.getUsername())
            );
            entryLog(
                    userIp,
                    user.getId(),
                    "2FA validation with username (" + user.getUsername() + ")",
                    false
            );
            throw new BadCredentialsException(I18n.get("exception.authentication_credentials"));
        }
    }

    private String getUserIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("x-forwarded-for");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    @Async
    protected void entryLog(String userIp, Long userId, String action, boolean success) {
        authLogService.save(
                new AuthLog.Builder()
                        .userIp(userIp)
                        .userId(userId)
                        .action(action)
                        .success(success)
                        .location(geoLocationService.getLocationByIp(userIp).orElse(null))
                        .build()
        );
    }

    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");
        return atIndex > 2 ? email.substring(0, 2) + "***" + email.substring(atIndex) : "hidden";
    }
}
