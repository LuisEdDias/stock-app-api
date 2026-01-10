package lat.luisdias.stock_app_main_service.stock.dto.box;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.stock.entities.box.BoxStatus;

public record StoreBoxDTO(
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long id,
        @NotBlank(message = "{validation.not_blank}")
        String name,
        @NotBlank(message = "{validation.not_blank}")
        String description,
        @NotNull(message = "{validation.box.status}")
        BoxStatus status,
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long inventoryId
) {
}
