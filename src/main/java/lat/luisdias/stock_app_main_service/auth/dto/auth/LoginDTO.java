package lat.luisdias.stock_app_main_service.auth.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDTO(
        @NotBlank(message = "{validation.not_blank}")
        @Email(message = "{validation.email.not_valid}")
        String email,
        @NotBlank(message = "{validation.not_blank}")
        String password
) {
}
