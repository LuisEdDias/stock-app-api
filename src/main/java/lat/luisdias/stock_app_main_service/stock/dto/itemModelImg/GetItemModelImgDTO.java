package lat.luisdias.stock_app_main_service.stock.dto.itemModelImg;

import lat.luisdias.stock_app_main_service.stock.entities.item.ItemImgLink;

import java.sql.Timestamp;

public record GetItemModelImgDTO(
        Long id,
        String link,
        Timestamp created
) {
    public GetItemModelImgDTO(ItemImgLink itemImgLink) {
        this(
                itemImgLink.getId(),
                itemImgLink.getStorageUrl() + itemImgLink.getStorageKey(),
                itemImgLink.getCreated());
    }
}
