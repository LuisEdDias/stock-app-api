package lat.luisdias.stock_app_main_service.security.authorization;

import lat.luisdias.stock_app_main_service.security.authorization.securitygroup.SecurityGroup;
import lat.luisdias.stock_app_main_service.security.identity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

public class AuthoritiesHandle {
    private AuthoritiesHandle() {
    }

    public static Collection<GrantedAuthority> extract(String role, Map<String, Set<String>> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        if (permissions == null || permissions.isEmpty()) {
            return authorities;
        }

        permissions.forEach((domain, actions) -> {
            if (actions.contains("*")) {
                authorities.add(new SimpleGrantedAuthority(domain + ":*"));
            } else {
                actions.forEach(action -> {
                    authorities.add(new SimpleGrantedAuthority(domain + ":" + action));
                });
            }
        });

        return authorities;
    }

    public static Map<String, Set<String>> permissionListFromGroup(SecurityGroup securityGroup) {
        Map<String, Set<String>> permissions = new HashMap<>();

        securityGroup.getPermissions().forEach(permission -> {
            String domain = permission.getDomain();
            String action = permission.getAction();

            permissions.compute(domain, (key, existing) -> {
                if (existing == null) {
                    Set<String> set = new HashSet<>();
                    set.add(action);
                    return set;
                }

                if (existing.contains("*")) {
                    return existing;
                }

                if ("*".equals(action)) {
                    Set<String> wildcard = new HashSet<>();
                    wildcard.add("*");
                    return wildcard;
                }

                existing.add(action);
                return existing;
            });
        });

        return permissions;
    }

    public static Map<String, Set<String>> permissionListFromUser(User user) {
        Map<String, Set<String>> userPermissions = new HashMap<>();

        Set<SecurityGroup> userGroups = user.getSecurityGroups();

        userGroups.forEach(group -> {
            Map<String, Set<String>> groupPermissions = permissionListFromGroup(group);

            groupPermissions.forEach((domain, actions) -> {
                userPermissions.compute(domain, (key, existing) -> {
                    if (existing == null) {
                        return new HashSet<>(actions);
                    }

                    if (existing.contains("*")) {
                        return existing;
                    }

                    if (actions.contains("*")) {
                        Set<String> wildcard = new HashSet<>();
                        wildcard.add("*");
                        return wildcard;
                    }

                    existing.addAll(actions);
                    return existing;
                });
            });
        });

        return userPermissions;
    }
}
