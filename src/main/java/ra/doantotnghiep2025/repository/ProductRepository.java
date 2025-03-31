package ra.doantotnghiep2025.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Products;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Products, Long> {
    boolean existsByProductName(String productName);
    List<Products> findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(String productName, String productDescription);
    Page<Products> findAll(Pageable pageable);
    List<Products> findTop10ByOrderBySoldQuantityDesc();
    List<Products> findTop10ByOrderByCreatedAtDesc();
    List<Products> findByOrderBySoldQuantityDesc(Pageable pageable);
    List<Products> findByCategoryCategoryId(Long categoryId, Pageable pageable);
    Optional<Products> findById(Long productId);
    List<Products> findTop10ByCreatedAtBetweenOrderByLikesDesc(LocalDateTime from, LocalDateTime to, Pageable pageable);
    List<Products> findByBrandBrandId(Long brandId, Pageable pageable);
    List<Products> findByCategoryCategoryId(Long categoryId);
}