package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByCategoryName(String categoryName);
}
