package lat.luisdias.stock_app_main_service.auth.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lat.luisdias.stock_app_main_service.auth.entities.user.UserRoles;

public record StoreUserDTO(
        @NotBlank(message = "{validation.not_blank}")
        @Email(message = "{validation.email.not_valid}")
        String email,
        @NotBlank(message = "{validation.not_blank}")
        String username,
        @NotNull(message = "{validation.user_role}")
        UserRoles role,
        @NotBlank(message = "{validation.not_blank}")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$*&@#])(?:([0-9a-zA-Z$*&@#])(?!\\1)){8,}$",
                message = "{validation.password.not_valid}"
        )
        String password,
        @NotBlank(message = "{validation.not_blank}")
        String confirmPassword
) {
}
