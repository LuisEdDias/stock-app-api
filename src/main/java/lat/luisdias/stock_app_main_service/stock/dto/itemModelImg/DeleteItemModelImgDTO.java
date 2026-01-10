package lat.luisdias.stock_app_main_service.stock.dto.itemModelImg;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record DeleteItemModelImgDTO(
        @NotEmpty(message = "{validation.not_empty}")
        List<Long> imgIds
) {
}
