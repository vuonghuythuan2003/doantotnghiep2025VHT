package ra.doantotnghiep2025.model.dto;

import lombok.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ShoppingCartResponseDTO {
    private Long shoppingCartId;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int orderQuantity;
    private String currency = "VND"; // Thêm thuộc tính currency, mặc định là VND
    private String image;

}