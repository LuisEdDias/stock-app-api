package lat.luisdias.stock_app_main_service.stock.dto.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

public record InventoryCheckDTO(
        @NotNull(message = "{validation.file.required}")
        MultipartFile file,
        @NotNull(message = "{validation.not_blank}")
        boolean boxedItemsOnly,
        @Positive(message = "{validation.only_positive_number}")
        Integer itemIdColumn
) {
}
