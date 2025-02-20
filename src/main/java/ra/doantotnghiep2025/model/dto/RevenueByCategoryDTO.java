package ra.doantotnghiep2025.model.dto;

import lombok.*;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class RevenueByCategoryDTO {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalRevenue;
}
