package lat.luisdias.stock_app_main_service.security.authentication;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Component
public class CustomJwtConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        UUID publicId = UUID.fromString(jwt.getSubject());
        String nickname = jwt.getClaimAsString("nickname");
        String role = jwt.getClaimAsString("role");
        List<String> permissions = jwt.getClaimAsStringList("permissions");

        if (role == null) {
            throw new BadJwtException("Missing role claim");
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(publicId, nickname, role);

        Collection<GrantedAuthority> authorities = extractAuthorities(role, permissions);

        return new UsernamePasswordAuthenticationToken(
                authenticatedUser,
                null,
                authorities
        );
    }

    private Collection<GrantedAuthority> extractAuthorities(String role, List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        if (permissions != null) {
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }

        return authorities;
    }
}
