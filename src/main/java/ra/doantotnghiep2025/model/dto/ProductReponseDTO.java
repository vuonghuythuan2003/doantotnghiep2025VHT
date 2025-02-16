package ra.doantotnghiep2025.model.dto;
import lombok.*;

import java.util.Date;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ProductReponseDTO {
    private Long productId;
    private String productName;
    private String sku;
    private String description;
    private Double unitPrice;
    private Integer stockQuantity;
    private Integer soldQuantity;
    private String image;
    private Long categoryId;
    private Date createdAt;
    private Date updatedAt;
}
