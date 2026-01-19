package lat.luisdias.stock_app_main_service.security.bootstrap;

import lat.luisdias.stock_app_main_service.security.identity.User;
import lat.luisdias.stock_app_main_service.security.authorization.UserRole;
import lat.luisdias.stock_app_main_service.security.identity.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RootUserBootstrapTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RootSecurityProperties properties;

    @InjectMocks
    private RootUserBootstrap bootstrap;

    @Test
    void shouldCreateRootUserWhenNoneExists() {
        when(userRepository.countAllByUserRole(UserRole.ROOT)).thenReturn(0);
        when(properties.getEmail()).thenReturn("root@test.com");
        when(properties.getPassword()).thenReturn("secret");
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        bootstrap.run(null);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldNotCreateRootUserWhenOneExists() {
        when(userRepository.countAllByUserRole(UserRole.ROOT)).thenReturn(1);

        bootstrap.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenMoreThanOneRootExists() {
        when(userRepository.countAllByUserRole(UserRole.ROOT)).thenReturn(2);

        assertThrows(
                IllegalStateException.class,
                () -> bootstrap.run(null)
        );
    }

    @Test
    void shouldFailWhenEmailIsMissing() {
        when(userRepository.countAllByUserRole(UserRole.ROOT)).thenReturn(0);
        when(properties.getEmail()).thenReturn("");

        assertThrows(
                IllegalStateException.class,
                () -> bootstrap.run(null)
        );
    }

    @Test
    void shouldFailWhenPasswordIsMissing() {
        when(userRepository.countAllByUserRole(UserRole.ROOT)).thenReturn(0);
        when(properties.getEmail()).thenReturn("root@test.com");
        when(properties.getPassword()).thenReturn("");

        assertThrows(
                IllegalStateException.class,
                () -> bootstrap.run(null)
        );
    }
}
