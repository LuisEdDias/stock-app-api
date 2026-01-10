package lat.luisdias.stock_app_main_service.stock.dto.itemModelImg;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record StoreItemModelImgDTO(
        @NotEmpty(message = "{validation.image.required}")
        List<MultipartFile> images
) {
}
