package lat.luisdias.stock_app_main_service.auth.entities.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;

public enum UserRoles {
    ROOT, ADMIN, USER, GUEST;

    @JsonCreator
    public static UserRoles fromString(String string) {
        for (UserRoles role : values()) {
            if (role.name().equalsIgnoreCase(string)) {
                return role;
            }
        }
        throw new IllegalArgumentException(I18n.get("validation.user_role"));
    }
}
