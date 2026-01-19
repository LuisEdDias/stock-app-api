package lat.luisdias.stock_app_main_service.security.authorization.config;

import lat.luisdias.stock_app_main_service.security.authorization.annotations.PublicAccess;
import lat.luisdias.stock_app_main_service.security.authorization.annotations.RequiresPermission;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    @Bean
    public Advisor requiresPremissionAdvisor(PermissionAuthorizationManager permissionAuthorizationManager) {
        Pointcut controllerPointcut = new AnnotationMatchingPointcut(null, RestController.class);
        Pointcut requiresPermissionPointcut = new AnnotationMatchingPointcut(null, RequiresPermission.class);
        Pointcut publicAccessPointcut = new AnnotationMatchingPointcut(PublicAccess.class);

        ComposablePointcut hybridPointcut = new ComposablePointcut(controllerPointcut)
                .union(requiresPermissionPointcut)
                .union(publicAccessPointcut);

        AuthorizationManagerBeforeMethodInterceptor interceptor = new AuthorizationManagerBeforeMethodInterceptor(
                hybridPointcut, permissionAuthorizationManager
        );

        interceptor.setOrder(AuthorizationInterceptorsOrder.FIRST.getOrder());

        return interceptor;
    }
}
