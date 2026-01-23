package lat.luisdias.stock_app_main_service.security.authorization.securitygroup.dto;

import lat.luisdias.stock_app_main_service.security.authorization.securitygroup.SecurityGroup;

public record GetSecurityGroupDTO(
        Long id,
        String name,
        String description
) {
    public GetSecurityGroupDTO(SecurityGroup securityGroup) {
        this(
                securityGroup.getId(),
                securityGroup.getName(),
                securityGroup.getDescription()
        );
    }
}
