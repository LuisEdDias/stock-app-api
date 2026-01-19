package lat.luisdias.stock_app_main_service.security.authentication;

import java.util.UUID;

public class AuthenticatedUser {
    private final UUID publicId;
    private final String nickname;
    private final String role;

    public AuthenticatedUser(UUID publicId, String nickname, String role) {
        this.publicId = publicId;
        this.nickname = nickname;
        this.role = role;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getRole() {
        return role;
    }
}
