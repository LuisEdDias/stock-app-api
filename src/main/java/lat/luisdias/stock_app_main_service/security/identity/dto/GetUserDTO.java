package lat.luisdias.stock_app_main_service.security.identity.dto;

import lat.luisdias.stock_app_main_service.security.authorization.securitygroup.dto.GetSecurityGroupDTO;
import lat.luisdias.stock_app_main_service.security.identity.User;

import java.util.List;
import java.util.stream.Collectors;

public record GetUserDTO(
        Long id,
        String subject,
        String email,
        String nickname,
        String role,
        List<GetSecurityGroupDTO> securityGroups
) {
    public GetUserDTO(User user){
        this(
                user.getId(),
                user.getSubject().toString(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getSecurityGroups()
                        .stream()
                        .map(GetSecurityGroupDTO::new)
                        .collect(Collectors.toList())
        );
    }
}
