package ra.doantotnghiep2025.model.dto;

import lombok.*;
import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderDetailResponseDTO {
    private Long id;
    private String name;
    private BigDecimal unitPrice;
    private int orderQuantity;
    private Long productId;
    private String productName;
}
