package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.Category;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.model.entity.Role;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.repository.CategoryRepository;
import ra.doantotnghiep2025.repository.ProductRepository;
import ra.doantotnghiep2025.repository.RoleRepository;
import ra.doantotnghiep2025.repository.UserRepository;
import ra.doantotnghiep2025.service.AdminService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service

public class AdminServiceImp implements AdminService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    public Page<UserResponseDTO> getUsers(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return userRepository.findAll(pageable).map(this::convertToDto);
    }
    @Override
    public UserRegisterResponseDTO updatePermission(Long userId, Long roleId) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("User NOT FOUND"));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomerException("Role NOT FOUND"));
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
            userRepository.save(user);
        }
        return UserRegisterResponseDTO.builder()
                .username(user.getUsername())
                .roles(user.getRoles())
                .build();
    }


    @Override
    public void deleteUserRole(Long userId, Long roleId) throws CustomerException {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomerException("Người dùng không tồn tại với ID này"));

        Role role = roleRepository.findById(roleId).orElseThrow(() -> new CustomerException("Người dùng không có quyền"));
        if(!user.getRoles().contains(role)) {
            throw new CustomerException("Người dùng không có quyền này chọn cái khác");
        }
        user.getRoles().remove(role);
        userRepository.save(user);
    }

    @Override
    public UserResponseDTO toggleUserStatus(Long userId) throws CustomerException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new CustomerException("Người dùng không tồn tại!");
        }

        User user = optionalUser.get();
        user.setStatus(!user.getStatus()); // Đảo trạng thái khóa/mở khóa
        userRepository.save(user);

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(user.getStatus())
                .build();
    }

    @Override
    public List<RoleResponseDTO> getRole() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(
                respon -> RoleResponseDTO.builder()
                        .id(respon.getRoleId())
                        .roleType(respon.getRoleName())
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> searchByUserName(String userName) {
        List<User> user = userRepository.findByFullnameContainingIgnoreCase(userName);
        return user.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public Page<ProductReponseDTO> getProducts(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).map(this::convertToProdcutDto);
    }

    @Override
    public ProductReponseDTO getProductById(Long productId) throws CustomerException {
        Products products = productRepository.findById(productId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy mã sản phẩm"));
        return convertToProdcutDto(products);
    }

    @Override
    public ProductReponseDTO saveProduct(ProductRequestDTO productRequestDTO) throws CustomerException {
        if (productRepository.existsByProductName(productRequestDTO.getProductName())) {
            throw new CustomerException("Tên sản phẩm đã tồn tại!");
        }

        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new CustomerException("Danh mục không tồn tại!"));

        Products product = Products.builder()
                .productName(productRequestDTO.getProductName())
                .productSku(productRequestDTO.getSku())
                .productDescription(productRequestDTO.getDescription())
                .productPrice(productRequestDTO.getUnitPrice())
                .productQuantity(productRequestDTO.getStockQuantity())
                .soldQuantity(productRequestDTO.getSoldQuantity() != null ? productRequestDTO.getSoldQuantity() : 0)
                .productImage(productRequestDTO.getImage())
                .category(category)
                .build();

        Products savedProduct = productRepository.save(product);

        return convertToProdcutDto(savedProduct);
    }

    @Override
    public ProductReponseDTO updateProductById(Long productId, ProductUpdateDTO productRequestDTO) throws CustomerException {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomerException("Mã sản phẩm không tồn tại"));

        if (!product.getProductName().equals(productRequestDTO.getProductName()) && productRepository.existsByProductName(productRequestDTO.getProductName())) {
            throw new CustomerException("Tên sản phẩm đã tồn tại");
        }

        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new CustomerException("Mã danh mục không tồn tại"));

        product.setProductName(productRequestDTO.getProductName());
        product.setProductSku(productRequestDTO.getSku());
        product.setProductDescription(productRequestDTO.getDescription());
        product.setProductPrice(productRequestDTO.getUnitPrice());
        product.setProductQuantity(productRequestDTO.getStockQuantity());
        product.setSoldQuantity(productRequestDTO.getSoldQuantity());
        product.setProductImage(productRequestDTO.getImage());
        product.setCategory(category);

        productRepository.save(product);

        return convertToProdcutDto(product);
    }


    @Override
    public boolean deleteProductById(Long productId) throws CustomerException {
        Products product = productRepository.findById(productId).orElseThrow( ()-> new CustomerException("Mã sản phẩm không tồn tại"));
        productRepository.delete(product);
        return true;
    }

    @Override
    public Page<CategoryResponseDTO> getCategories(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return categoryRepository.findAll(pageable).map(this::convertToCategoryDto);
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long categoryId) throws CustomerException {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new CustomerException("Mã danh mục không tồn tại"));
        return convertToCategoryDto(category);
    }

    @Override
    public CategoryResponseDTO saveCategory(CategoryRequestDTO categoryRequestDTO) throws CustomerException {
        Category category = Category.builder()
                .categoryName(categoryRequestDTO.getCategoryName())
                .categoryDescription(categoryRequestDTO.getDescription())
                .status(true)
                .build();
        Category savedCategory = categoryRepository.save(category);
        return convertToCategoryDto(savedCategory);
    }

    @Override
    public CategoryResponseDTO updateCategory(Long categoryId, CategoryUpdateDTO categoryRequestDTO) throws CustomerException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomerException("Category not found"));

        category.setCategoryName(categoryRequestDTO.getCategoryName());
        category.setCategoryDescription(categoryRequestDTO.getDescription());
        category.setStatus(categoryRequestDTO.getStatus());

        Category updatedCategory = categoryRepository.save(category);
        return convertToCategoryDto(updatedCategory);
    }

    @Override
    public boolean deleteCategoryById(Long categoryId) throws CustomerException {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new CustomerException("Không tìm thấy mã danh mục để xóa"));
        categoryRepository.delete(category);
        return true;
    }

    @Override
    public List<User> getNewAccountsThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        return userRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);
    }

    private CategoryResponseDTO convertToCategoryDto(Category category) {
        return CategoryResponseDTO.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getCategoryDescription())
                .status(category.isStatus())
                .build();
    }


    private ProductReponseDTO convertToProdcutDto(Products product) {
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

    private UserResponseDTO convertToDto(User user) {
        return UserResponseDTO.builder().id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(user.getStatus())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
    
}
