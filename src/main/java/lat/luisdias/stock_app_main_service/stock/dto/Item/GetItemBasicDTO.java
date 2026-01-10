package lat.luisdias.stock_app_main_service.stock.dto.Item;

import lat.luisdias.stock_app_main_service.stock.entities.item.Item;

import java.sql.Timestamp;

public record GetItemBasicDTO(
        Long id,
        String model,
        String comment,
        String status,
        boolean available,
        Timestamp created,
        Timestamp updated
) {
    public GetItemBasicDTO(Item item) {
        this(
                item.getId(),
                item.getItemModel().getModel(),
                item.getComment(),
                item.getStatus().name(),
                item.isAvailable(),
                item.getCreated(),
                item.getUpdated()
        );
    }
}
