package ra.doantotnghiep2025.model.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name="categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name="category_name", nullable = false, unique = true, length = 100)
    private String categoryName;

    @Column(name="category_description", length = 255)
    private String categoryDescription;

    @Column(name = "status", nullable = false, columnDefinition = "bit DEFAULT true")
    private boolean status = true;
}
