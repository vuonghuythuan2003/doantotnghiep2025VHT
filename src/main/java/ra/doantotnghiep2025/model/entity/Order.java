package ra.doantotnghiep2025.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "serial_number", nullable = false, unique = true, length = 100, updatable = false)
    private String serialNumber = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.WAITING; // Mặc định là WAITING

    @Column(name = "note", length = 100)
    private String note;

    @Column(name = "receive_name", nullable = false, length = 100)
    private String receiveName;

    @Column(name = "receive_address", nullable = false, length = 255)
    private String receiveAddress;

    @Column(name = "receive_phone", nullable = false, length = 15)
    private String receivePhone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderDetail> orderDetails;

    @PrePersist
    protected void onCreate() {
        this.receivedAt = createdAt.plusDays(4);
    }
}
