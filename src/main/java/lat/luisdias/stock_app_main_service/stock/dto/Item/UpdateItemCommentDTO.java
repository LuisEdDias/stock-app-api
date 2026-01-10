package lat.luisdias.stock_app_main_service.stock.dto.Item;

import jakarta.validation.constraints.NotBlank;

public record UpdateItemCommentDTO(
        @NotBlank(message = "{validation.not_blank}")
        String comment
) {
}
