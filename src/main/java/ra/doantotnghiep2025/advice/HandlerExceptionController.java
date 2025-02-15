package ra.doantotnghiep2025.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ra.doantotnghiep2025.exception.CustomerException;

import java.util.HashMap;
import java.util.Map;

public class HandlerExceptionController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DataError<Map<String,String>> handlerValidException(MethodArgumentNotValidException exception) {
        Map<String,String> maps = new HashMap<>();
        exception.getFieldErrors().forEach(
                fieldError -> maps.put(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                )
        );
        return new DataError<>(400,maps);

    }
    @ExceptionHandler(CustomerException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public DataError<?> handlerCustomException(CustomerException exception) {
        return new DataError<>(404,exception.getMessage());
    }
}