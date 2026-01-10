package lat.luisdias.stock_app_main_service.auth.infra.security;

import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.repositories.user.UserRepository;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {
    private final UserRepository userRepository;

    public CustomJwtConverter(
            final UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        User user;
        try {
             user = userRepository.findById(Long.valueOf(jwt.getSubject()))
                    .orElseThrow();
        } catch (Exception e) {
            throw new BadJwtException(I18n.get("exception.invalid_token"));
        }
        return new UsernamePasswordAuthenticationToken(user, jwt.getTokenValue(), user.getAuthorities());
    }
}