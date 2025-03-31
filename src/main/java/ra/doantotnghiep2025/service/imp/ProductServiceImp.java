package ra.doantotnghiep2025.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.MostLikedProductDTO;
import ra.doantotnghiep2025.model.dto.ProductReponseDTO;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.repository.BrandRepository;
import ra.doantotnghiep2025.repository.CategoryRepository;
import ra.doantotnghiep2025.repository.ProductRepository;
import ra.doantotnghiep2025.service.ProductService;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImp implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;

    @Override
    public List<ProductReponseDTO> searchProducts(String keyword) {
        List<Products> products = productRepository.findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(keyword, keyword);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<ProductReponseDTO> getProducts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public List<ProductReponseDTO> getFeaturedProducts() {
        List<Products> products = productRepository.findTop10ByOrderBySoldQuantityDesc();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductReponseDTO> getNewProducts() {
        List<Products> products = productRepository.findTop10ByOrderByCreatedAtDesc();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProductReponseDTO> getBestSellerProducts(int limit) {
        List<Products> products = productRepository.findByOrderBySoldQuantityDesc(PageRequest.of(0, limit));
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<ProductReponseDTO> getProductsByCategory(Long categoryId, int page, int size) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Danh mục không tồn tại");
        }
        if (!categoryRepository.findById(categoryId).get().isStatus()) {
            throw new RuntimeException("Danh mục không hoạt động");
        }
        Pageable pageable = PageRequest.of(page, size);
        List<Products> products = productRepository.findByCategoryCategoryId(categoryId, pageable);
        List<ProductReponseDTO> productDTOs = products.stream().map(this::convertToDTO).collect(Collectors.toList());
        int totalElements = productRepository.findByCategoryCategoryId(categoryId).size();
        return new PageImpl<>(productDTOs, pageable, totalElements);
    }
    @Override
    public List<ProductReponseDTO> getProductsByBrand(Long brandId, int page, int size) {
        if (!brandRepository.existsById(brandId)) {
            throw new RuntimeException("Thương hiệu không tồn tại");
        }
        if (!brandRepository.findById(brandId).get().getStatus()) {
            throw new RuntimeException("Thương hiệu không hoạt động");
        }

        Pageable pageable = PageRequest.of(page, size);
        List<Products> products = productRepository.findByBrandBrandId(brandId, pageable);
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductReponseDTO getProductById(Long productId) throws CustomerException {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomerException("Sản phẩm không tồn tại với ID: " + productId));
        return convertToDTO(product);
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


    private ProductReponseDTO convertToDTO(Products product) {
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
                .brandId(product.getBrand().getBrandId())
                .createdAt(java.util.Date.from(product.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()))
                .updatedAt(product.getUpdatedAt() != null ? java.util.Date.from(product.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()) : null)
                .build();
    }
}
