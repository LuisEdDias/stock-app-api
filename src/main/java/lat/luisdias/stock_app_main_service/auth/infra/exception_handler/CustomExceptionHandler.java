package lat.luisdias.stock_app_main_service.auth.infra.exception_handler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import lat.luisdias.stock_app_main_service.auth.dto.message.ErrorMessageDTO;
import lat.luisdias.stock_app_main_service.auth.dto.message.FieldErrorMessageDTO;
import lat.luisdias.stock_app_main_service.stock.infra.util.I18n;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Void> error404() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<FieldErrorMessageDTO>> error400(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(e.getFieldErrors().stream().map(FieldErrorMessageDTO::new).toList());
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorMessageDTO> error401() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorMessageDTO(I18n.get("exception.authentication_credentials")));
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorMessageDTO> error400(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorMessageDTO(e.getMessage()));
    }

    @ExceptionHandler(InternalError.class)
    public ResponseEntity<ErrorMessageDTO> error500() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessageDTO> error409() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorMessageDTO(I18n.get("exception.delete_referenced_entity")));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorMessageDTO> error500(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessageDTO(e.getMessage()));
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<ErrorMessageDTO> error400(PersistenceException e) {
        return ResponseEntity.badRequest().body(new ErrorMessageDTO(e.getMessage()));
    }
}
