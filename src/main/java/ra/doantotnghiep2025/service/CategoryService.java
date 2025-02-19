package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.doantotnghiep2025.model.dto.CategoryResponseDTO;
import ra.doantotnghiep2025.model.dto.ProductReponseDTO;
import ra.doantotnghiep2025.model.entity.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAllActiveCategories();
}
