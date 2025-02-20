package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.Role;
import ra.doantotnghiep2025.model.entity.User;

import java.util.List;

public interface AdminService {
    Page<UserResponseDTO> getUsers(int page, int size, String sortBy, String direction);
    UserRegisterResponseDTO updatePermission(Long userId, Long roleId) throws CustomerException;
    void deleteUserRole(Long userId, Long roleId) throws CustomerException;
    UserResponseDTO toggleUserStatus(Long userId) throws CustomerException;
    List<RoleResponseDTO> getRole();
    List<UserResponseDTO> searchByUserName(String userName);
    Page<ProductReponseDTO> getProducts(int page, int size, String sortBy, String direction);
    ProductReponseDTO getProductById(Long productId) throws CustomerException;
    ProductReponseDTO saveProduct(ProductRequestDTO productRequestDTO) throws CustomerException;
    ProductReponseDTO updateProductById(Long productId, ProductUpdateDTO productRequestDTO) throws CustomerException;
    boolean deleteProductById(Long productId) throws CustomerException;
    Page<CategoryResponseDTO> getCategories(int page, int size, String sortBy, String direction);
    CategoryResponseDTO getCategoryById(Long categoryId) throws CustomerException;
    CategoryResponseDTO saveCategory(CategoryRequestDTO categoryRequestDTO) throws CustomerException;
    CategoryResponseDTO updateCategory(Long categoryId, CategoryUpdateDTO categoryRequestDTO) throws CustomerException;
    boolean deleteCategoryById(Long categoryId) throws CustomerException;
    List<User> getNewAccountsThisMonth();

}

