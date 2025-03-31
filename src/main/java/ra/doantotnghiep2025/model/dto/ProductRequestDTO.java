package ra.doantotnghiep2025.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import ra.doantotnghiep2025.validator.ProductNameUnique;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductRequestDTO {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 100, message = "Tên sản phẩm không được vượt quá 100 ký tự")
    @ProductNameUnique(message = "Tên sản phẩm không được trùng lặp")
    private String productName;

    @NotBlank(message = "SKU không được để trống")
    @Size(max = 100, message = "SKU không được vượt quá 100 ký tự")
    private String sku;

    private String description;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
    private Double unitPrice;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    @Min(value = 0, message = "Số lượng bán không được âm")
    private Integer soldQuantity;

    private MultipartFile image;

    @NotNull(message = "Mã danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Mã thương hiệu không được để trống")
    private Long brandId;
}
