package lat.luisdias.stock_app_main_service.auth.services.auth;

import io.jsonwebtoken.Jwts;
import lat.luisdias.stock_app_main_service.auth.entities.user.User;
import lat.luisdias.stock_app_main_service.auth.infra.util.KeyUtil;
import lat.luisdias.stock_app_main_service.auth.repositories.user.UserRepository;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;

    private final KeyUtil keyUtil;

    public AuthService(final UserRepository userRepository, final KeyUtil keyUtil) {
        this.userRepository = userRepository;
        this.keyUtil = keyUtil;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(I18n.get("exception.authentication_credentials")));
    }

    public String getToken(User user) {
        try {
            return Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("authorities", user.getAuthorities())
                    .claim("requires2fa", false)
                    .claim("name", user.getNickname())
                    .issuedAt(new Date())
                    .expiration(expireTime(720))
                    .signWith(this.keyUtil.loadPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        } catch (Exception exception) {
            throw new RuntimeException(I18n.get("exception.token_generation_error"));
        }
    }

    public String getPartialToken(User user) {
        try {
            return Jwts.builder()
                    .subject(user.getId().toString())
                    .claim("authorities", user.getAuthorities())
                    .claim("requires2fa", true)
                    .claim("profile-name", user.getNickname())
                    .issuedAt(new Date())
                    .expiration(expireTime(10))
                    .signWith(this.keyUtil.loadPrivateKey(), Jwts.SIG.RS256)
                    .compact();
        } catch (Exception exception) {
            throw new RuntimeException(I18n.get("exception.token_generation_error"));
        }
    }

    private Date expireTime(int time) {
        Instant future = Instant.now().plus(Duration.ofMinutes(time));
        return Date.from(future);
    }
}
