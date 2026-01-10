package lat.luisdias.stock_app_main_service.auth.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPasswordDTO(
        @NotBlank(message = "{validation.not_blank}")
        String password
) {
}
