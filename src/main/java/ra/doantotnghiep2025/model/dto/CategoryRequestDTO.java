package ra.doantotnghiep2025.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ra.doantotnghiep2025.validator.CategoryNameUnique;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryRequestDTO {
    @CategoryNameUnique(message = "Tên danh mục đã tồn tại ")
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String categoryName;

    private String description;
    
}
