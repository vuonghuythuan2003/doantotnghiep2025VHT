package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService {
    Page<UserResponseDTO> getAllUsers(Pageable pageable); // Consolidated method for fetching users
    UserRegisterResponseDTO updatePermission(Long userId, Long roleId) throws CustomerException;
    void deleteUserRole(Long userId, Long roleId) throws CustomerException;
    List<RoleResponseDTO> getRole();
    List<UserResponseDTO> searchByUserName(String userName);
    Page<ProductReponseDTO> getProducts(Pageable pageable); // Updated to use Pageable
    ProductReponseDTO getProductById(Long productId) throws CustomerException;
    ProductReponseDTO saveProduct(ProductRequestDTO productRequestDTO) throws CustomerException;
    ProductReponseDTO updateProductById(Long productId, ProductUpdateDTO productRequestDTO) throws CustomerException;
    void deleteProductById(Long productId) throws CustomerException;
    Page<CategoryResponseDTO> getCategories(Pageable pageable); // Updated to use Pageable
    CategoryResponseDTO getCategoryById(Long categoryId) throws CustomerException;
    CategoryResponseDTO saveCategory(CategoryRequestDTO categoryRequestDTO) throws CustomerException;
    CategoryResponseDTO updateCategory(Long categoryId, CategoryUpdateDTO categoryRequestDTO) throws CustomerException;
    boolean deleteCategoryById(Long categoryId) throws CustomerException;
    List<User> getNewAccountsThisMonth();
    void addRoleToUser(Long userId, Long roleId) throws CustomerException;
    void removeRoleFromUser(Long userId, Long roleId) throws CustomerException;
    void toggleUserStatus(Long userId, boolean status) throws CustomerException;


}