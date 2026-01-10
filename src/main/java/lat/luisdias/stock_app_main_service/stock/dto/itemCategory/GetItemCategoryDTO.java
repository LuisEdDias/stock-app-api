package lat.luisdias.stock_app_main_service.stock.dto.itemCategory;

import lat.luisdias.stock_app_main_service.stock.entities.item.ItemCategory;

import java.sql.Timestamp;

public record GetItemCategoryDTO(
        long id,
        String name,
        String description,
        Timestamp created,
        Timestamp updated
) {
    public GetItemCategoryDTO(ItemCategory itemCategory) {
        this(
                itemCategory.getId(),
                itemCategory.getName(),
                itemCategory.getDescription(),
                itemCategory.getCreated(),
                itemCategory.getUpdated()
        );
    }
}
