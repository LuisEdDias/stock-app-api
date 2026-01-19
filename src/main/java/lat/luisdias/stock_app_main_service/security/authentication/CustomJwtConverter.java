package lat.luisdias.stock_app_main_service.security.authentication;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomJwtConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        UUID publicId;

        try {
            publicId = UUID.fromString(jwt.getSubject());
        } catch (IllegalArgumentException e) {
            throw new BadJwtException("Invalid subject UUID", e);
        }

        String nickname = jwt.getClaimAsString("nickname");
        String role = jwt.getClaimAsString("role");
        Map<String, List<String>> permissions = jwt.getClaim("perm");

        if (role == null) {
            throw new BadJwtException("Missing role claim");
        }

        if (nickname == null) {
            throw new BadJwtException("Missing nickname claim");
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(publicId, nickname, role);

        Collection<GrantedAuthority> authorities = extractAuthorities(role, permissions);

        return new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                authorities
        );
    }

    private Collection<GrantedAuthority> extractAuthorities(String role, Map<String, List<String>> permissions) {
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
}
