package ra.doantotnghiep2025.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "products")
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "sku", unique = true, nullable = false, length = 100)
    private String productSku;

    @Column(name = "product_name", unique = true, nullable = false, length = 100)
    private String productName;

    @Column(name = "product_description", columnDefinition = "text")
    private String productDescription;

    @Column(name = "product_price", nullable = false)
    private double productPrice;

    @Min(0)
    @Column(name = "product_quantity", nullable = false)
    private int productQuantity;

    @Column(name = "product_image", length = 255)
    private String productImage;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category category;

    // Gán UUID & createdAt khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        this.productSku = UUID.randomUUID().toString();
        this.createdAt = new Date();
    }

    // Cập nhật thời gian khi có chỉnh sửa
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Date();
    }
}
