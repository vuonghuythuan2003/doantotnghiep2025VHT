package ra.doantotnghiep2025.model.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDTO {
    private String fullname;
    private String phone;
    private MultipartFile avatar; // Sử dụng MultipartFile để xử lý tệp
    private String address;
}