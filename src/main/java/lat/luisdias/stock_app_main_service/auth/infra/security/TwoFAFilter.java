package lat.luisdias.stock_app_main_service.auth.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lat.luisdias.stock_app_main_service.auth.infra.util.KeyUtil;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Component
public class TwoFAFilter extends OncePerRequestFilter {
    private final KeyUtil keyUtil;
    private static final List<String> ALLOWED_PATHS = List.of(
            "/login/validate-2fa"
    );

    public TwoFAFilter(
            final KeyUtil keyUtil
    ) {
        this.keyUtil = keyUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                Jwt decodedToken = getJwtDecoder().decode(token);

                if (ALLOWED_PATHS.stream().anyMatch(requestPath::endsWith)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                boolean requires2FA = decodedToken.getClaimAsBoolean("requires2fa");

                if (requires2FA) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write(I18n.get("exception.2fa_required"));
                    return;
                }
            }
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(I18n.get("exception.invalid_token"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private JwtDecoder getJwtDecoder() {
        RSAPublicKey publicKey = keyUtil.loadPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
