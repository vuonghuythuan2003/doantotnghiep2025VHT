package ra.doantotnghiep2025.model.dto;

import lombok.*;
import ra.doantotnghiep2025.model.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private String serialNumber;
    private Long userId;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String note;
    private String receiveName;
    private String receiveAddress;
    private String receivePhone;
    private LocalDateTime createdAt;
    private LocalDateTime receivedAt;
    private String payUrl;
    private List<OrderDetailResponseDTO> items;
}
