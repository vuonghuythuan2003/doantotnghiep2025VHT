package ra.doantotnghiep2025.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.advice.DataError;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.service.AuthService;
import ra.doantotnghiep2025.service.TokenService;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private TokenService tokenService;

       @PostMapping("/sign-up")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequestDTO requestDTO, BindingResult bindingResult) throws CustomerException {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(new DataError<>(400, errors));
        }

        UserRegisterResponseDTO responseDTO = authService.register(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDto requestDTO, BindingResult bindingResult) throws CustomerException {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(new DataError<>(400, errors));
        }

        UserLoginResponse responseDTO = authService.login(requestDTO);
        System.out.println("Phản hồi từ login: " + responseDTO); // Logging dữ liệu trả về
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(new DataError<>(400, "Token không hợp lệ hoặc không tồn tại"));
        }

        token = token.substring(7);
        tokenService.invalidateToken(token);
        return ResponseEntity.ok(new DataError<>(200, "Đăng xuất thành công"));
    }

}
