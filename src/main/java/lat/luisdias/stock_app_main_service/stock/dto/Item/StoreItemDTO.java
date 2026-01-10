package lat.luisdias.stock_app_main_service.stock.dto.Item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemStatus;

public record StoreItemDTO(
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long id,
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long modelId,
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long inventoryId,
        @NotBlank(message = "{validation.not_blank}")
        String comment,
        @NotNull(message = "{validation.item.status}")
        ItemStatus status,
        @Positive(message = "{validation.only_positive_number}")
        Long boxId
) {
}
