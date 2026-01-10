package lat.luisdias.stock_app_main_service.stock.dto.ItemModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record StoreItemModelDTO(
        @NotBlank(message = "{validation.not_blank}")
        String model,
        @NotBlank(message = "{validation.not_blank}")
        String description,
        @NotNull(message = "{validation.not_blank}")
        @Positive(message = "{validation.only_positive_number}")
        Long categoryId,
        List<MultipartFile> images
) {
}
