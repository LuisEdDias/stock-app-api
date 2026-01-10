package lat.luisdias.stock_app_main_service.stock.dto.ItemModel;

import lat.luisdias.stock_app_main_service.stock.dto.itemModelImg.GetItemModelImgDTO;
import lat.luisdias.stock_app_main_service.stock.entities.item.ItemModel;

import java.sql.Timestamp;
import java.util.List;

public record GetItemModelWithImagesDTO(
        Long id,
        String model,
        String category,
        String description,
        boolean available,
        Timestamp created,
        Timestamp updated,
        List<GetItemModelImgDTO> images
) {
    public GetItemModelWithImagesDTO(ItemModel itemModel) {
        this(
                itemModel.getId(),
                itemModel.getModel(),
                itemModel.getCategory().getName(),
                itemModel.getDescription(),
                itemModel.isActive(),
                itemModel.getCreated(),
                itemModel.getUpdated(),
                itemModel.getImgLinks().stream().map(GetItemModelImgDTO::new).toList()
        );
    }
}
