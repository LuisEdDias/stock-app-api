package lat.luisdias.stock_app_main_service.stock.dto.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lat.luisdias.stock_app_main_service.stock.dto.address.StoreAddressDTO;

public record StoreInventoryDTO(
        @NotBlank(message = "{validation.not_blank}")
        String name,
        @Valid
        StoreAddressDTO address
) {
}
