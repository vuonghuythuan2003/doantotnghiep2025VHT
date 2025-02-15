package ra.doantotnghiep2025.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import ra.doantotnghiep2025.validator.UsernameUnique;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserLoginRequestDto {
    @NotBlank(message = "Tên người dùng không được để trống")
    @Size(min = 6, max = 100, message = "Tên người dùng phải từ 6 - 100 kí tự")
    @Pattern(regexp = "^[a-zA-z0-9]+$", message = "Tên người dùng chỉ được chứa chữ cái và số")
    @UsernameUnique
    private String username;
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}