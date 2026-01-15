package lat.luisdias.stock_app_main_service.security.infra.security;

import lat.luisdias.stock_app_main_service.security.entities.user.UserRole;
import lat.luisdias.stock_app_main_service.security.infra.util.KeyUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfigurations {
    private final CustomJwtConverter authoritiesConverter;
    private final KeyUtil keyUtil;
    private final TwoFAFilter twoFAFilter;

    public SecurityConfigurations(final CustomJwtConverter authoritiesConverter, final KeyUtil keyUtil, final TwoFAFilter twoFAFilter) {
        this.authoritiesConverter = authoritiesConverter;
        this.keyUtil = keyUtil;
        this.twoFAFilter = twoFAFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::configureAuthorization)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(authoritiesConverter))
                )
                .addFilterBefore(twoFAFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth.requestMatchers(HttpMethod.POST, "/v1/login").permitAll();
        auth.requestMatchers(HttpMethod.POST, "/v1/user/create-root").permitAll();
        auth.requestMatchers(HttpMethod.GET, "/v1/item-category/**").authenticated();
        auth.requestMatchers(HttpMethod.GET, "/v1/item-model/**").authenticated();
        auth.requestMatchers(HttpMethod.GET, "/v1/inventory/**").authenticated();
        auth.requestMatchers(HttpMethod.POST, "/v1/inventory/*/check").authenticated();
        auth.requestMatchers("/v1/user/**").authenticated();
        auth.requestMatchers("/v1/item/**").authenticated();
        auth.requestMatchers("/v1/box/**").authenticated();
        auth.anyRequest().hasAnyAuthority(UserRole.ROOT.toString(), UserRole.ADMIN.toString());
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        RSAPublicKey publicKey = keyUtil.loadPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
