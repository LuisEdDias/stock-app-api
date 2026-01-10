package lat.luisdias.stock_app_main_service.stock.dto.box;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MoveBoxDTO(
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long inventoryId
) {
}
