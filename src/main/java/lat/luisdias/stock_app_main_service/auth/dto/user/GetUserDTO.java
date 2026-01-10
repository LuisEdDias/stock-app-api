package lat.luisdias.stock_app_main_service.auth.dto.user;

import lat.luisdias.stock_app_main_service.auth.entities.user.User;

import java.util.List;

public record GetUserDTO(
        Long id,
        String username,
        String nickname,
        List<String> role
) {
    public GetUserDTO(User user){
        this(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAuthorities().stream().map(Object::toString).toList()
        );
    }
}
