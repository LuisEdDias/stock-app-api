package lat.luisdias.stock_app_main_service.security.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UpdateEmailDTO(
        @NotBlank
        UUID subject,
        @NotBlank(message = "{validation.not_blank}")
        String password,
        @NotBlank(message = "{validation.not_blank}")
        @Email(message = "{validation.email.not_valid}")
        String email
) {
}