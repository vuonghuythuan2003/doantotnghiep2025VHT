package ra.doantotnghiep2025.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Mã xác nhận không được để trống")
    private String token;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}
