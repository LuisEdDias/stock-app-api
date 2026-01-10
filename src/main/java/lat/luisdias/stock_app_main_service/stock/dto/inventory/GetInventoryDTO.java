package lat.luisdias.stock_app_main_service.stock.dto.inventory;

import lat.luisdias.stock_app_main_service.stock.dto.address.GetAddressDTO;
import lat.luisdias.stock_app_main_service.stock.entities.inventory.Inventory;

public record GetInventoryDTO(
        Long id,
        String name,
        boolean active,
        GetAddressDTO address
) {
    public GetInventoryDTO(Inventory inventory) {
        this(
                inventory.getId(),
                inventory.getName(),
                inventory.isActive(),
                inventory.getAddress() != null ?
                new GetAddressDTO(inventory.getAddress()) : null
        );
    }
}
