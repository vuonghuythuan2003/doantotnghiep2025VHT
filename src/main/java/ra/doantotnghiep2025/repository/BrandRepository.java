package ra.doantotnghiep2025.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Brand;
import ra.doantotnghiep2025.model.entity.Products;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByBrandName(String brandName);

    Optional<Brand> findById(Long brandId);

    List<Brand> findByBrandNameContainingIgnoreCase(String brandName);

    Page<Brand> findAll(Pageable pageable);

    List<Brand> findByCategoryCategoryId(Long categoryId, Pageable pageable);

}