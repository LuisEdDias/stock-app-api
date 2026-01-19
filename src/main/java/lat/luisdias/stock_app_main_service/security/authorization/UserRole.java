package lat.luisdias.stock_app_main_service.security.authorization;

import com.fasterxml.jackson.annotation.JsonCreator;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;

public enum UserRole {
    ROOT, ADMIN, USER, GUEST;

    @JsonCreator
    public static UserRole fromString(String string) {
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(string)) {
                return role;
            }
        }
        throw new IllegalArgumentException(I18n.get("validation.user_role"));
    }
}
