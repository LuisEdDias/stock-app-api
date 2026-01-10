package lat.luisdias.stock_app_main_service.auth.dto.message;

import org.springframework.validation.FieldError;

public record FieldErrorMessageDTO(String field, String message) {
    public FieldErrorMessageDTO(FieldError error) {
        this(error.getField(), error.getDefaultMessage());
    }
}
