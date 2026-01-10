package lat.luisdias.stock_app_main_service.stock.dto.box;

import lat.luisdias.stock_app_main_service.stock.entities.box.Box;

import java.sql.Timestamp;

public record GetBoxDTO(
        Long id,
        String name,
        InventoryDTO inventory,
        String description,
        String status,
        Timestamp created,
        Timestamp updated
) {
    public GetBoxDTO(Box box) {
        this(
                box.getId(),
                box.getName(),
                new InventoryDTO(box.getInventory().getId(), box.getInventory().getName()),
                box.getDescription(),
                box.getStatus().name(),
                box.getCreated(),
                box.getUpdated()
        );
    }

    private record InventoryDTO(Long id, String name) {
    }
}
