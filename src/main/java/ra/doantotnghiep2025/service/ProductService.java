package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.MostLikedProductDTO;
import ra.doantotnghiep2025.model.dto.ProductReponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductService {
    List<ProductReponseDTO> searchProducts(String keyword);
    Page<ProductReponseDTO> getProducts(int page, int size, String sortBy, String direction);
    List<ProductReponseDTO> getFeaturedProducts();
    List<ProductReponseDTO> getNewProducts();
    List<ProductReponseDTO> getBestSellerProducts(int limit);
    Page<ProductReponseDTO> getProductsByCategory(Long categoryId, int page, int size);    ProductReponseDTO getProductById(Long productId) throws CustomerException;
    List<MostLikedProductDTO> getMostLikedProducts(LocalDateTime from, LocalDateTime to);
    List<ProductReponseDTO> getProductsByBrand(Long brandId, int page, int size);
}
