package ra.doantotnghiep2025.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.MostLikedProductDTO;
import ra.doantotnghiep2025.model.dto.ProductReponseDTO;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.repository.CategoryRepository;
import ra.doantotnghiep2025.repository.ProductRepository;
import ra.doantotnghiep2025.service.ProductService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImp implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<ProductReponseDTO> searchProducts(String keyword) {
        List<Products> products = productRepository.findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(keyword, keyword);
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public Page<ProductReponseDTO> getProducts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public List<ProductReponseDTO> getFeaturedProducts() {
        List<Products> products = productRepository.findTop10ByOrderBySoldQuantityDesc();
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductReponseDTO> getNewProducts() {
        List<Products> products = productRepository.findTop10ByOrderByCreatedAtDesc();
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductReponseDTO> getBestSellerProducts(int limit) {
        List<Products> products = productRepository.findByOrderByProductQuantityDesc(PageRequest.of(0, limit));
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductReponseDTO> getProductsByCategory(Long categoryId, int page, int size) {
        List<Products> products = productRepository.findByCategoryCategoryId(categoryId, PageRequest.of(page, size));
        return products.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public ProductReponseDTO getProductById(Long productId) throws CustomerException {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomerException("Sản phẩm không tồn tại với ID: " + productId));
        return convertToDto(product);
    }

    @Override
    public List<MostLikedProductDTO> getMostLikedProducts(LocalDateTime from, LocalDateTime to) {
        return productRepository.findTop10ByCreatedAtBetweenOrderByLikesDesc(from, to, PageRequest.of(0, 10))
                .stream()
                .map(product -> MostLikedProductDTO.builder()
                        .productId(product.getProductId())
                        .productName(product.getProductName())
                        .totalLikes(product.getLikes())
                        .build())
                .collect(Collectors.toList());
    }


    private ProductReponseDTO convertToDto(Products product) {
        return ProductReponseDTO.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .sku(product.getProductSku())
                .description(product.getProductDescription())
                .unitPrice(product.getProductPrice())
                .stockQuantity(product.getProductQuantity())
                .soldQuantity(product.getSoldQuantity())
                .image(product.getProductImage())
                .categoryId(product.getCategory().getCategoryId())
                .createdAt(java.sql.Timestamp.valueOf(product.getCreatedAt()))
                .updatedAt(product.getUpdatedAt() != null ? java.sql.Timestamp.valueOf(product.getUpdatedAt()) : null)
                .build();
    }
}
