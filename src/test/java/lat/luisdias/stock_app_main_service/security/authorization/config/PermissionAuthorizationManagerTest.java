package lat.luisdias.stock_app_main_service.security.authorization.config;

import lat.luisdias.stock_app_main_service.security.authorization.annotations.PublicAccess;
import lat.luisdias.stock_app_main_service.security.authorization.annotations.RequiresPermission;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionAuthorizationManagerTest {

    private PermissionAuthorizationManager permissionAuthorizationManager;
    private MethodInvocation methodInvocation;

    @BeforeEach
    public void setUp() {
        permissionAuthorizationManager = new PermissionAuthorizationManager();
        methodInvocation = mock(MethodInvocation.class);
    }

    @Test
    void shouldGrantAccessForRoot() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "root", null, List.of(new SimpleGrantedAuthority("ROLE_ROOT")));

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    void shouldDenyAccessIfNotAuthenticated() throws NoSuchMethodException {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        Method publicMethod = TestControllerMethods.class.getMethod("publicEndpoint");
        when(methodInvocation.getMethod()).thenReturn(publicMethod);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    void shouldDenyAccessIfAuthenticationIsNull() throws NoSuchMethodException {
        Method publicMethod = TestControllerMethods.class.getMethod("publicEndpoint");
        when(methodInvocation.getMethod()).thenReturn(publicMethod);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> null, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    void shouldGrantAccessForPublicAnnotation() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of()
        );

        Method publicMethod = TestControllerMethods.class.getMethod("publicEndpoint");
        when(methodInvocation.getMethod()).thenReturn(publicMethod);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    void shouldGrantAccessWithSpecificPermission() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("DOMAIN:ACTION"))
        );

        Method securedMethod = TestControllerMethods.class.getMethod("securedEndpoint");
        when(methodInvocation.getMethod()).thenReturn(securedMethod);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    void shouldGrantAccessWithWildcardPermission() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("DOMAIN:*"))
        );

        Method securedMethod = TestControllerMethods.class.getMethod("securedEndpoint");
        when(methodInvocation.getMethod()).thenReturn(securedMethod);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    void shouldDenyWildcardWhenPermissionHasNoDomain() throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ADMIN:*"))
        );

        Method method = TestControllerMethods.class.getMethod("adminOnly");
        when(methodInvocation.getMethod()).thenReturn(method);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    void shouldDenyAccessIfPermissionNotMatched() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("DOMAIN:OTHER"))
        );

        Method securedMethod = TestControllerMethods.class.getMethod("securedEndpoint");
        when(methodInvocation.getMethod()).thenReturn(securedMethod);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    void shouldDenyAccessWhenAnnotationIsMissing() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("DOMAIN:ACTION"))
        );

        Method methodWithoutAnnotation = TestControllerMethods.class.getMethod("denyByDefault");
        when(methodInvocation.getMethod()).thenReturn(methodWithoutAnnotation);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    void shouldGrantAccessWhenAnnotationIsMissingAndUserIsRoot() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "root", null, List.of(new SimpleGrantedAuthority("ROLE_ROOT"))
        );

        Method methodWithoutAnnotation = TestControllerMethods.class.getMethod("denyByDefault");
        when(methodInvocation.getMethod()).thenReturn(methodWithoutAnnotation);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    void shouldRequiresPermissionTakePrecedenceOverPublicAnnotation() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of()
        );

        Method annotationPrecedenceMethod = TestControllerMethods.class.getMethod("annotationPrecedence");
        when(methodInvocation.getMethod()).thenReturn(annotationPrecedenceMethod);

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    void shouldGrantAccessWhenControllerIsPublicAndMethodNotAnnotated() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of()
        );

        Method methodWithoutAnnotation = TestPublicControllerAnnotation.class.getMethod("publicEndpoint");
        when(methodInvocation.getMethod()).thenReturn(methodWithoutAnnotation);
        when(methodInvocation.getThis()).thenReturn(new TestPublicControllerAnnotation());

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    void shouldDenyAccessWhenControllerIsPublicAndMethodRequiresPermission() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of()
        );

        Method securedMethod = TestPublicControllerAnnotation.class.getMethod("securedEndpoint");
        when(methodInvocation.getMethod()).thenReturn(securedMethod);
        when(methodInvocation.getThis()).thenReturn(new TestPublicControllerAnnotation());

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @Test
    void shouldGrantAccessWhenClassIsSecuredAndUserHasPermission() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("DOMAIN:ACTION"))
        );

        Method publicMethod = TestRequiresPermissionAnnotation.class.getMethod("publicEndpoint");
        when(methodInvocation.getMethod()).thenReturn(publicMethod);
        when(methodInvocation.getThis()).thenReturn(new TestRequiresPermissionAnnotation());

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertTrue(decision.isGranted());
    }

    @Test
    void shouldDenyAccessWhenClassIsSecuredAndPermissionNotMatch() throws NoSuchMethodException {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("DOMAIN:OTHER"))
        );

        Method publicMethod = TestRequiresPermissionAnnotation.class.getMethod("publicEndpoint");
        when(methodInvocation.getMethod()).thenReturn(publicMethod);
        when(methodInvocation.getThis()).thenReturn(new TestRequiresPermissionAnnotation());

        AuthorizationDecision decision = permissionAuthorizationManager.check(() -> auth, methodInvocation);

        assertNotNull(decision);
        assertFalse(decision.isGranted());
    }

    @RestController
    static class TestControllerMethods {
        @PublicAccess
        public void publicEndpoint() {}

        @RequiresPermission("DOMAIN:ACTION")
        public void securedEndpoint() {}

        public void denyByDefault() {}

        @RequiresPermission("DOMAIN:ACTION")
        @PublicAccess
        public void annotationPrecedence() {}

        @RequiresPermission("ROLE_ADMIN")
        public void adminOnly() {}
    }

    @RestController
    @PublicAccess
    static class TestPublicControllerAnnotation {
        public void publicEndpoint() {}

        @RequiresPermission("DOMAIN:ACTION")
        public void securedEndpoint() {}
    }

    @RequiresPermission("DOMAIN:ACTION")
    static class TestRequiresPermissionAnnotation {
        public void publicEndpoint() {}
    }
}
