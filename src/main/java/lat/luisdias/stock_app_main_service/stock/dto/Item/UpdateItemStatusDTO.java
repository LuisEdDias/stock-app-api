package lat.luisdias.stock_app_main_service.stock.dto.Item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemStatus;

public record UpdateItemStatusDTO(
        @NotBlank(message = "{validation.not_blank}")
        String comment,
        @NotNull(message = "{validation.item.status}")
        ItemStatus status
) {
}
