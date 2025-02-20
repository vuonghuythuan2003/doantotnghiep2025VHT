package ra.doantotnghiep2025.model.dto;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BestSellerProductDTO {
    private Long productId;
    private String productName;
    private int totalQuantitySold;
    private BigDecimal totalRevenue;
}
