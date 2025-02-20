package ra.doantotnghiep2025.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ra.doantotnghiep2025.model.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class OrderHistoryResponseDTO {
    private Long orderId;
    private String serialNumber;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderDetailResponseDTO> orderDetails;
}
    