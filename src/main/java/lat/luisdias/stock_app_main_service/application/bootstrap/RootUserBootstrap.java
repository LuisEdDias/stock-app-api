package lat.luisdias.stock_app_main_service.application.bootstrap;

import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.entities.user.UserRole;
import lat.luisdias.stock_app_main_service.auth.repositories.user.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class RootUserBootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RootSecurityProperties properties;

    public RootUserBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RootSecurityProperties properties
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(UserRole.ROOT)) {
            return;
        }
        validateProperties();
        User rootUser = new User(
                UUID.randomUUID(),
                properties.getEmail(),
                passwordEncoder.encode(properties.getPassword()),
                "SUPER USER",
                UserRole.ROOT
        );

        userRepository.save(rootUser);
    }

    private void validateProperties() {
        if (!StringUtils.hasText(properties.getEmail())) {
            throw new IllegalStateException("ROOT email not configured");
        }
        if (!StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException("ROOT password not configured");
        }
    }
}