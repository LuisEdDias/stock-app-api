package lat.luisdias.stock_app_main_service.application.bootstrap;

import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.entities.user.UserRole;
import lat.luisdias.stock_app_main_service.auth.repositories.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Profile("!test")
@Component
public class RootUserBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(RootUserBootstrap.class);
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
        int rootUsers = userRepository.countAllByUserRole(UserRole.ROOT);
        switch (rootUsers) {
            case 0:
                validateProperties();
                createRootUser();
                log.info("Root user created");
                log.warn("ROOT user created with credentials from configuration. Change the password immediately.");
                break;
            case 1:
                log.info("Root user found");
                break;
            default:
                throw new IllegalStateException("More than one ROOT user found");
        }
    }

    private void createRootUser() {
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