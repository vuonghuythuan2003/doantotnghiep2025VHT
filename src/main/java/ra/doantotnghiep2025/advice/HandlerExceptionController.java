package ra.doantotnghiep2025.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ra.doantotnghiep2025.exception.CustomerException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class HandlerExceptionController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataError<Map<String, String>>> handlerValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getFieldErrors().forEach(
                fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage())
        );
        return ResponseEntity.badRequest().body(new DataError<>(400, errors));
    }

    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<DataError<String>> handlerCustomException(CustomerException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DataError<>(404, exception.getMessage()));
    }

}
