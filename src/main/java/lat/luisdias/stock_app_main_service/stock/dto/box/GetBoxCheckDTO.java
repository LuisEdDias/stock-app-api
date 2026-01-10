package lat.luisdias.stock_app_main_service.stock.dto.box;

import lat.luisdias.stock_app_main_service.stock.dto.Item.GetItemBasicDTO;

import java.util.List;

public record GetBoxCheckDTO(
        List<GetItemBasicDTO> itemsNotFound,
        List<Long> unboxedItems
) {
}
