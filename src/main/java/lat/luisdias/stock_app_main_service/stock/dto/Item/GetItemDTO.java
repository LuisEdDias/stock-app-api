package lat.luisdias.stock_app_main_service.stock.dto.Item;

import lat.luisdias.stock_app_main_service.stock.entities.item.Item;

import java.sql.Timestamp;

public record GetItemDTO(
        Long id,
        ItemModelDTO itemModel,
        BoxDTO box,
        InventoryDTO inventory,
        Long inventoryId,
        String comment,
        String status,
        boolean available,
        Timestamp created,
        Timestamp updated
) {
    public GetItemDTO(Item item) {
        this(
                item.getId(),
                new ItemModelDTO(item.getItemModel().getId(), item.getItemModel().getModel()),
                item.getBox() == null ? null : new BoxDTO(item.getBox().getId(), item.getBox().getName()),
                new InventoryDTO(item.getInventory().getId(), item.getInventory().getName()),
                item.getInventory().getId(),
                item.getComment(),
                item.getStatus().name(),
                item.isAvailable(),
                item.getCreated(),
                item.getUpdated()
        );
    }

    public record ItemModelDTO(Long id, String model) {}
    public record InventoryDTO(Long id, String name) {}
    public record BoxDTO(Long id, String name) {}
}
