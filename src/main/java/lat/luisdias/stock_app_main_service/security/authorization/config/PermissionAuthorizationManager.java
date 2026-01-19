package lat.luisdias.stock_app_main_service.security.authorization.config;

import lat.luisdias.stock_app_main_service.security.authorization.UserRole;
import lat.luisdias.stock_app_main_service.security.authorization.annotations.PublicAccess;
import lat.luisdias.stock_app_main_service.security.authorization.annotations.RequiresPermission;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class PermissionAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authenticationSupplier, MethodInvocation methodInvocation) {

        Authentication auth = authenticationSupplier.get();

        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        if (auth.getAuthorities()
                .contains(
                        new SimpleGrantedAuthority("ROLE_" + UserRole.ROOT.name())
                )
        ) {
            return new AuthorizationDecision(true);
        }

        Annotation annotation = getResolvedAnnotation(methodInvocation);

        if (annotation instanceof PublicAccess) {
            return new AuthorizationDecision(true);
        }

        if (annotation instanceof RequiresPermission) {
            return new AuthorizationDecision(hasPermission(auth, ((RequiresPermission) annotation).value()));
        }

        return new AuthorizationDecision(false);
    }

    private boolean hasPermission(Authentication auth, String requiredPermission) {
        Set<String> userAuthorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (userAuthorities.contains(requiredPermission)) {
            return true;
        }

        if (requiredPermission.contains(":")) {
            String domain = requiredPermission.split(":")[0];
            return userAuthorities.contains(domain + ":*");
        }

        return false;
    }

    private Annotation getResolvedAnnotation(MethodInvocation methodInvocation) {
        Annotation annotation = AnnotationUtils.findAnnotation(methodInvocation.getMethod(), RequiresPermission.class);

        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(methodInvocation.getMethod(), PublicAccess.class);
        }

        if (annotation == null && methodInvocation.getThis() != null) {
            annotation = AnnotationUtils.findAnnotation(methodInvocation.getThis().getClass(), RequiresPermission.class);
            if (annotation == null) {
                annotation = AnnotationUtils.findAnnotation(methodInvocation.getThis().getClass(), PublicAccess.class);
            }
        }

        return annotation;
    }
}
