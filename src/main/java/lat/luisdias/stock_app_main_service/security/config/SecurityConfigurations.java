package lat.luisdias.stock_app_main_service.security.config;

import lat.luisdias.stock_app_main_service.security.authentication.CustomJwtConverter;
import lat.luisdias.stock_app_main_service.security.authentication.two.factor.auth.TwoFactorAuthFilter;
import lat.luisdias.stock_app_main_service.security.authentication.KeyLoad;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
public class SecurityConfigurations {
    private final CustomJwtConverter customJwtConverter;
    private final KeyLoad keyLoad;
    private final TwoFactorAuthFilter twoFactorAuthFilter;

    public SecurityConfigurations(
            final CustomJwtConverter customJwtConverter,
            final KeyLoad keyLoad,
            final TwoFactorAuthFilter twoFactorAuthFilter
    ) {
        this.customJwtConverter = customJwtConverter;
        this.keyLoad = keyLoad;
        this.twoFactorAuthFilter = twoFactorAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(this::configureAuthorization)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtConverter))
                )
                .addFilterBefore(twoFactorAuthFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth.requestMatchers(HttpMethod.POST, "/v1/login").permitAll();
        auth.anyRequest().authenticated();
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
        RSAPublicKey publicKey = keyLoad.loadPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
