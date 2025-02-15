package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Products;

public interface ProductRepository extends JpaRepository<Products, Long> {
    boolean existsByProductName(String productName);
}