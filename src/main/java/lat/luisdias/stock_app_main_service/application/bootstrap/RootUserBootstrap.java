package lat.luisdias.stock_app_main_service.application.bootstrap;

import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.entities.user.UserRole;
import lat.luisdias.stock_app_main_service.auth.repositories.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class RootUserBootstrap implements ApplicationRunner {
    private final UserRepository userRepository;
    @Value("${app.security.root.email}")
    private String rootEmail;
    @Value("${app.security.root.password}")
    private String rootPassword;

    public RootUserBootstrap(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.existsByRole(UserRole.ROOT)) {
            return;
        }

        User rootUser = new User(
                UUID.randomUUID(),
                rootEmail,
                rootPassword,
                "SUPER USER",
                UserRole.ROOT
        );

        userRepository.save(rootUser);
    }
}
