package lat.luisdias.stock_app_main_service.security.authorization;

import lat.luisdias.stock_app_main_service.security.authorization.securitygroup.SecurityGroup;
import lat.luisdias.stock_app_main_service.security.identity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

public class AuthoritiesHandle {
    private AuthoritiesHandle(){}

    public static Collection<GrantedAuthority> extract(String role, Map<String, Set<String>> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        if (permissions != null) {
            permissions.forEach((domain, actions) -> {
                if (actions.contains("*")) {
                    authorities.add(new SimpleGrantedAuthority(domain + ":*"));
                } else {
                    actions.forEach(action -> {
                        authorities.add(new SimpleGrantedAuthority(domain + ":" + action));
                    });
                }
            });
        }

        return authorities;
    }

    public static Map<String, Set<String>> permissionListFromGroup(SecurityGroup securityGroup) {
        Map<String, Set<String>> permissions = new HashMap<>();

        securityGroup.getPermissions().forEach(permission -> {
            String domain = permission.getDomain();
            String action = permission.getAction();

            if (permissions.containsKey(domain) && !permissions.get(domain).contains("*")) {
                if (permissions.containsKey(domain)) {
                    permissions.get(domain).add(action);
                } else {
                    permissions.put(domain, Set.of(action));
                }
            } else {
                permissions.put(domain, Set.of("*"));
            }
        });

        return permissions;
    }

    public static Map<String, Set<String>> permissionListFromUser(User user) {
        Map<String, Set<String>> userPermissions = new HashMap<>();

        Set<SecurityGroup> userGroups = user.getSecurityGroups();

        userGroups.forEach(group -> {
            Map<String, Set<String>> groupPermissions = permissionListFromGroup(group);

            groupPermissions.forEach((domain, actions) -> {
                if (userPermissions.containsKey(domain) && !userPermissions.get(domain).contains("*")) {
                    if (actions.contains("*")) {
                        userPermissions.put(domain, Set.of("*"));
                    } else if (userPermissions.containsKey(domain)) {
                        userPermissions.get(domain).addAll(actions);
                    } else {
                        userPermissions.put(domain, actions);
                    }
                }
            });
        });

        return userPermissions;
    }
}
