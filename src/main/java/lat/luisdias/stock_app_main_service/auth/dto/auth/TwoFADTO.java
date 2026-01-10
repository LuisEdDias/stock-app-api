package lat.luisdias.stock_app_main_service.auth.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TwoFADTO(
        @NotBlank(message = "{validation.not_blank}")
        @Pattern(regexp = "\\d{6}", message = "{validation.totpcode}")
        String totpCode
) {
}
