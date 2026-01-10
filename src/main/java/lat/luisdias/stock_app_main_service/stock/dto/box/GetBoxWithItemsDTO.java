package lat.luisdias.stock_app_main_service.stock.dto.box;

import lat.luisdias.stock_app_main_service.stock.dto.Item.GetItemBasicDTO;
import lat.luisdias.stock_app_main_service.stock.entities.box.Box;

import java.sql.Timestamp;
import java.util.List;

public record GetBoxWithItemsDTO(
        Long id,
        String name,
        InventoryDTO inventory,
        String description,
        String status,
        Timestamp created,
        Timestamp updated,
        List<GetItemBasicDTO> items
) {
    public GetBoxWithItemsDTO(Box box) {
        this(
                box.getId(),
                box.getName(),
                new InventoryDTO(box.getInventory().getId(), box.getInventory().getName()),
                box.getDescription(),
                box.getStatus().name(),
                box.getCreated(),
                box.getUpdated(),
                box.getItems().stream().map(GetItemBasicDTO::new).toList()
        );
    }

    private record InventoryDTO (Long id, String name) {
    }
}
