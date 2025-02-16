package ra.doantotnghiep2025.service;

import ra.doantotnghiep2025.model.dto.CategoryResponseDTO;
import ra.doantotnghiep2025.model.entity.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryResponseDTO> getAllActiveCategories();
}
