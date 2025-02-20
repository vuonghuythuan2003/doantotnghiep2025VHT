package ra.doantotnghiep2025.model.dto;

import lombok.*;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TopSpendingCustomerDTO {
    private Long userId;
    private String fullName;
    private String email;
    private BigDecimal totalSpent;
}
