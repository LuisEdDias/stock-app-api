package lat.luisdias.stock_app_main_service.stock.dto.Item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateItemModelDTO(
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long newModelId,
        @NotBlank(message = "{validation.not_blank}")
        String comment
) {
}
