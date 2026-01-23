package lat.luisdias.stock_app_main_service.security.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record UpdatePasswordDTO(
        @NotBlank
        UUID subject,
        @NotBlank(message = "{validation.not_blank}")
        String oldPassword,
        @NotBlank(message = "{validation.not_blank}")
        @Pattern(
                regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[$*&@#])(?:([0-9a-zA-Z$*&@#])(?!\\1)){8,}$",
                message = "{validation.password.not_valid}"
        )
        String newPassword,
        @NotBlank(message = "{validation.not_blank}")
        String confirmPassword
) {
}