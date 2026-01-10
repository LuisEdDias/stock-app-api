package lat.luisdias.stock_app_main_service.auth.infra.util;

import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Component
 public abstract class AuthenticatedUser {
    public static User getCurrentUser(@AuthenticationPrincipal User user) {
        return user;
    }
}
