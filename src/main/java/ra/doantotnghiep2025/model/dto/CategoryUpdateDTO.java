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
public class CategoryUpdateDTO {
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    @CategoryNameUnique(message = "Tên danh mục không được trùng lặp")
    private String categoryName;

    private String description;
    private Boolean status;
}
