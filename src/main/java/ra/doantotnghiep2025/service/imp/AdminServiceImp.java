package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.*;
import ra.doantotnghiep2025.repository.*;
import ra.doantotnghiep2025.service.AdminService;
import ra.doantotnghiep2025.service.UploadFileService;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
    @Autowired
    private UploadFileService uploadFileService;
    @Autowired
    private BrandRepository brandRepository;

    @Override
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::convertToDto);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại với ID này"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomerException("Người dùng không có quyền"));
        if (!user.getRoles().contains(role)) {
            throw new CustomerException("Người dùng không có quyền này, chọn cái khác");
        }
        user.getRoles().remove(role);
        userRepository.save(user);
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
        List<User> users = userRepository.findByFullnameContainingIgnoreCase(userName);
        return users.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public Page<ProductReponseDTO> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::convertToProdcutDto);
    }

    @Override
    public ProductReponseDTO getProductById(Long productId) throws CustomerException {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy mã sản phẩm"));
        return convertToProdcutDto(product);
    }

    @Override
    public ProductReponseDTO saveProduct(ProductRequestDTO productRequestDTO) throws CustomerException {
        if (productRepository.existsByProductName(productRequestDTO.getProductName())) {
            throw new CustomerException("Tên sản phẩm đã tồn tại!");
        }

        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new CustomerException("Danh mục không tồn tại!"));

        // Kiểm tra thương hiệu
        Brand brand = brandRepository.findById(productRequestDTO.getBrandId())
                .orElseThrow(() -> new CustomerException("Thương hiệu không tồn tại!"));

        String fileUrl = null;
        if (productRequestDTO.getImage() != null && !productRequestDTO.getImage().isEmpty()) {
            try {
                fileUrl = uploadFileService.uploadFile(productRequestDTO.getImage());
            } catch (RuntimeException e) {
                throw new CustomerException("Lỗi khi upload ảnh sản phẩm: " + e.getMessage());
            }
        }

        Products product = Products.builder()
                .productName(productRequestDTO.getProductName())
                .productSku(productRequestDTO.getSku())
                .productDescription(productRequestDTO.getDescription())
                .productPrice(productRequestDTO.getUnitPrice())
                .productQuantity(productRequestDTO.getStockQuantity())
                .soldQuantity(productRequestDTO.getSoldQuantity() != null ? productRequestDTO.getSoldQuantity() : 0)
                .productImage(fileUrl)
                .category(category)
                .brand(brand) // Gán thương hiệu cho sản phẩm
                .build();

        Products savedProduct = productRepository.save(product);
        return convertToProdcutDto(savedProduct);
    }

    @Override
    public ProductReponseDTO updateProductById(Long productId, ProductUpdateDTO productRequestDTO) throws CustomerException {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomerException("Mã sản phẩm không tồn tại"));

        if (!product.getProductName().equals(productRequestDTO.getProductName()) &&
                productRepository.existsByProductName(productRequestDTO.getProductName())) {
            throw new CustomerException("Tên sản phẩm đã tồn tại");
        }

        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new CustomerException("Mã danh mục không tồn tại"));

        String fileUrl = product.getProductImage();
        if (productRequestDTO.getImage() != null && !productRequestDTO.getImage().isEmpty()) {
            try {
                if (!productRequestDTO.getImage().getContentType().startsWith("image/")) {
                    throw new CustomerException("File tải lên không phải định dạng ảnh hợp lệ");
                }
                fileUrl = uploadFileService.uploadFile(productRequestDTO.getImage());
            } catch (RuntimeException e) {
                throw new CustomerException("Lỗi khi upload ảnh sản phẩm: " + e.getMessage());
            }
        }

        product.setProductName(productRequestDTO.getProductName());
        product.setProductSku(productRequestDTO.getSku());
        product.setProductDescription(productRequestDTO.getDescription());
        product.setProductPrice(productRequestDTO.getUnitPrice());
        product.setProductQuantity(productRequestDTO.getStockQuantity());
        product.setSoldQuantity(productRequestDTO.getSoldQuantity());
        product.setProductImage(fileUrl);
        product.setCategory(category);

        productRepository.save(product);
        return convertToProdcutDto(product);
    }

    @Override
    public void deleteProductById(Long productId) throws CustomerException {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomerException("Mã sản phẩm không tồn tại"));
        productRepository.delete(product);
    }

    @Override
    public Page<CategoryResponseDTO> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::convertToCategoryDto);
    }

    @Override
    public CategoryResponseDTO getCategoryById(Long categoryId) throws CustomerException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomerException("Mã danh mục không tồn tại"));
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
                .orElseThrow(() -> new CustomerException("Không tìm thấy danh mục"));

        category.setCategoryName(categoryRequestDTO.getCategoryName());
        category.setCategoryDescription(categoryRequestDTO.getDescription());
        category.setStatus(categoryRequestDTO.getStatus());

        Category updatedCategory = categoryRepository.save(category);
        return convertToCategoryDto(updatedCategory);
    }

    @Override
    public boolean deleteCategoryById(Long categoryId) throws CustomerException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy mã danh mục để xóa"));

        List<Products> products = productRepository.findByCategoryCategoryId(categoryId);
        if (!products.isEmpty()) {
            throw new CustomerException("Không thể xóa danh mục vì danh mục đang chứa sản phẩm.");
        }

        categoryRepository.delete(category);
        return true;
    }

    @Override
    public List<User> getNewAccountsThisMonth() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);
        return userRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);
    }

    @Override
    @Transactional
    public void addRoleToUser(Long userId, Long roleId) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomerException("Vai trò không tồn tại"));

        if (user.getRoles().contains(role)) {
            throw new CustomerException("Người dùng đã có vai trò này");
        }

        user.getRoles().add(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new CustomerException("Vai trò không tồn tại"));

        if (!user.getRoles().contains(role)) {
            throw new CustomerException("Người dùng không có vai trò này");
        }

        if (user.getRoles().size() <= 1) {
            throw new CustomerException("Không thể xóa vai trò cuối cùng của người dùng");
        }

        user.getRoles().remove(role);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId, boolean status) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        if (user.getStatus() == status) {
            throw new CustomerException("Trạng thái của người dùng đã ở trạng thái này");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
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
                .brandId(product.getBrand() != null ? product.getBrand().getBrandId() : null)
                .createdAt(Timestamp.valueOf(product.getCreatedAt()))
                .updatedAt(product.getUpdatedAt() != null ? Timestamp.valueOf(product.getUpdatedAt()) : null)
                .build();
    }

    private UserResponseDTO convertToDto(User user) {
        // Map roles to RoleDTO
        List<RoleDTO> roleDTOs = user.getRoles() != null ? user.getRoles().stream().map(role -> {
            RoleDTO roleDTO = new RoleDTO();
            roleDTO.setId(role.getRoleId());
            roleDTO.setRoleType(role.getRoleName().name());
            return roleDTO;
        }).collect(Collectors.toList()) : List.of();

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(user.getStatus())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .address(user.getAddress())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .roles(roleDTOs) // Add roles here
                .build();
    }
}