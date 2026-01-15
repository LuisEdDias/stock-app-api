package lat.luisdias.stock_app_main_service.security.dto.user;

import lat.luisdias.stock_app_main_service.security.identity.entities.User;

import java.util.List;

public record GetUserDTO(
        Long id,
        String publicId,
        String username,
        String nickname,
        String role,
        List<String> permissions
) {
    public GetUserDTO(User user){
        this(
                user.getId(),
                user.getPublicId().toString(),
                user.getUsername(),
                user.getNickname(),
                user.getRole().name(),
                user.getAuthorities().stream().map(Object::toString).toList()
        );
    }
}