package ra.doantotnghiep2025.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategoryName(String categoryName);
    List<Category> findByStatusTrue();
    Page<Category> findAll(Pageable pageable);
}
