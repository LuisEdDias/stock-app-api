package lat.luisdias.stock_app_main_service.stock.dto.Item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ItemIdListDTO(
        @NotEmpty(message = "{validation.not_empty}")
        List<Long> items,
        @NotBlank(message = "{validation.not_blank}")
        String comment
) {
}
