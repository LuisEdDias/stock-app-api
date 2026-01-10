package lat.luisdias.stock_app_main_service.stock.dto.ItemModel;

import lat.luisdias.stock_app_main_service.stock.entities.item.ItemModel;

import java.sql.Timestamp;

public record GetItemModelDTO(
        Long id,
        String model,
        String category,
        String description,
        boolean available,
        Timestamp created,
        Timestamp updated
) {
    public GetItemModelDTO(ItemModel itemModel) {
        this(
                itemModel.getId(),
                itemModel.getModel(),
                itemModel.getCategory().getName(),
                itemModel.getDescription(),
                itemModel.isActive(),
                itemModel.getCreated(),
                itemModel.getUpdated()
        );
    }
}
