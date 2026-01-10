package lat.luisdias.stock_app_main_service.auth.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordDTO(
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
