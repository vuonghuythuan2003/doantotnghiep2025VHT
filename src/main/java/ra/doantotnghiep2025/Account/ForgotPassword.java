package ra.doantotnghiep2025.Account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.ForgotPasswordRequestDTO;
import ra.doantotnghiep2025.model.dto.ResetPasswordRequestDTO;
import ra.doantotnghiep2025.service.AuthService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ForgotPassword {

    private final AuthService userService;

    @PostMapping("/account/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) throws CustomerException {
        userService.forgotPassword(request);
        return ResponseEntity.ok("Email khôi phục mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn.");
    }

    @PostMapping("/account/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) throws CustomerException {
        userService.resetPassword(request);
        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công.");
    }
}