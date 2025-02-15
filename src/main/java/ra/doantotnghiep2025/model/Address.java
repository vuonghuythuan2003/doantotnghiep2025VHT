package ra.doantotnghiep2025.model;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_address", nullable = false, length = 255)
    private String fullAddress;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "receive_name", nullable = false, length = 50)
    private String receiveName;
}
