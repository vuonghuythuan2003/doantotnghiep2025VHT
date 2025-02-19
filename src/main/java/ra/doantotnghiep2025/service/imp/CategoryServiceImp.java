package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.model.dto.CategoryResponseDTO;
import ra.doantotnghiep2025.model.entity.Category;
import ra.doantotnghiep2025.repository.CategoryRepository;
import ra.doantotnghiep2025.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImp implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponseDTO> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findByStatusTrue()
                .stream()
                .filter(Category::isStatus)
                .collect(Collectors.toList());

        return categories.stream().map(category -> CategoryResponseDTO.builder()
                        .categoryId(category.getCategoryId())
                        .categoryName(category.getCategoryName())
                        .description(category.getCategoryDescription())
                        .status(category.isStatus())
                        .build())
                .collect(Collectors.toList());
    }


}
