package lat.luisdias.stock_app_main_service.stock.dto.itemCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoreItemCategoryDTO(
        @NotBlank(message = "{validation.not_blank}")
        @Pattern(regexp = "^[a-zA-Z ]+$", message = "{validation.only_letters}")
        @Size(min = 3, max = 15, message = "{validation.size}")
        String name,
        @NotBlank(message = "{validation.not_blank}")
        String description
) {
}
