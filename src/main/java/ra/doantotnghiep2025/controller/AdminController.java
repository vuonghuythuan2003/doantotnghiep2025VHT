// File: src/main/java/ra/doantotnghiep2025/controller/AdminController.java
package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.OrderStatus;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.service.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        String[] validSortFields = {"id", "username", "email", "fullname", "createdAt", "updatedAt"};
        if (!Arrays.asList(validSortFields).contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy + ". Valid fields are: " + String.join(", ", validSortFields));
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponseDTO> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/role/{roleId}")
    public ResponseEntity<String> addRoleToUser(
            @Valid
            @PathVariable Long userId,
            @PathVariable Long roleId) throws CustomerException {
        adminService.addRoleToUser(userId, roleId);
        return ResponseEntity.ok("Thêm quyền thành công");
    }

    @DeleteMapping("/users/{userId}/role/{roleId}")
    public ResponseEntity<String> removeRoleFromUser(
            @Valid
            @PathVariable Long userId,
            @PathVariable Long roleId) throws CustomerException {
        adminService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok("Xóa quyền thành công");
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<String> toggleUserStatus(
            @Valid
            @PathVariable Long userId,
            @RequestParam boolean status) throws CustomerException {
        adminService.toggleUserStatus(userId, status);
        return ResponseEntity.ok(status ? "Mở khóa người dùng thành công" : "Khóa người dùng thành công");
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDTO>> getRoles() {
        List<RoleResponseDTO> roleRespon = adminService.getRole();
        return ResponseEntity.ok(roleRespon);
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(@Valid @RequestParam String username) {
        List<UserResponseDTO> userRespon = adminService.searchByUserName(username);
        return ResponseEntity.ok(userRespon);
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductReponseDTO>> getProducts(
            @Valid
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        String[] validSortFields = {"productId", "productName", "productPrice", "createdAt"};
        if (!Arrays.asList(validSortFields).contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy + ". Valid fields are: " + String.join(", ", validSortFields));
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(adminService.getProducts(pageable));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductReponseDTO> getProduct(@Valid @PathVariable Long productId) throws CustomerException {
        ProductReponseDTO product = adminService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductReponseDTO> createProduct(@ModelAttribute @Valid ProductRequestDTO productRequestDTO) throws CustomerException {
        ProductReponseDTO newProduct = adminService.saveProduct(productRequestDTO);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @PutMapping(value = "/products/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductReponseDTO> updateProduct(
            @PathVariable Long productId,
            @Valid @ModelAttribute ProductUpdateDTO productRequestDTO) throws CustomerException {
        try {
            ProductReponseDTO updatedProduct = adminService.updateProductById(productId, productRequestDTO);
            return ResponseEntity.ok(updatedProduct);
        } catch (CustomerException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomerException("Lỗi không xác định khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> deleteProduct(@Valid @PathVariable Long productId) throws CustomerException {
        logger.info("Received DELETE request for productId: {}", productId);
        adminService.deleteProductById(productId);
        logger.info("Product with productId: {} deleted successfully", productId);
        return ResponseEntity.ok("Xóa sản phẩm thành công");
    }

    @GetMapping("/categories")
    public ResponseEntity<Page<CategoryResponseDTO>> getCategories(
            @Valid
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "categoryName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        String[] validSortFields = {"categoryId", "categoryName"};
        if (!Arrays.asList(validSortFields).contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy + ". Valid fields are: " + String.join(", ", validSortFields));
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoryResponseDTO> categories = adminService.getCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long categoryId) throws CustomerException {
        CategoryResponseDTO category = adminService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody @Valid CategoryRequestDTO categoryRequestDTO) throws CustomerException {
        CategoryResponseDTO createdCategory = adminService.saveCategory(categoryRequestDTO);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long categoryId,
                                                              @RequestBody @Valid CategoryUpdateDTO categoryUpdateDTO) throws CustomerException {
        CategoryResponseDTO updatedCategory = adminService.updateCategory(categoryId, categoryUpdateDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        try {
            boolean isDeleted = adminService.deleteCategoryById(categoryId);
            if (isDeleted) {
                return ResponseEntity.ok("Danh mục đã được xóa thành công!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không thể xóa danh mục.");
            }
        } catch (CustomerException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi khi xóa danh mục: " + e.getMessage());
        }
    }
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
            @Valid
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        String[] validSortFields = {"orderId", "serialNumber", "totalPrice", "createdAt", "receivedAt"};
        if (!Arrays.asList(validSortFields).contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy + ". Valid fields are: " + String.join(", ", validSortFields));
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/status/{orderStatus}")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(
            @Valid
            @PathVariable OrderStatus orderStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        String[] validSortFields = {"orderId", "serialNumber", "totalPrice", "createdAt", "receivedAt"};
        if (!Arrays.asList(validSortFields).contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy + ". Valid fields are: " + String.join(", ", validSortFields));
        }

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderResponseDTO> orders = orderService.getOrdersByStatus(orderStatus, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@Valid @PathVariable Long orderId) throws CustomerException {
        OrderResponseDTO orderResponse = orderService.getOrderById(orderId);
        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody @Valid OrderStatus orderStatus) throws CustomerException {
        OrderResponseDTO updatedOrder = orderService.updateOrderStatus(orderId, orderStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/sales-revenue-over-time")
    public ResponseEntity<BigDecimal> getSalesRevenueOverTime(
            @Valid
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(orderService.getSalesRevenueOverTime(from, to));
    }

    @GetMapping("/reports/revenue-over-time")
    public ResponseEntity<List<RevenueOverTimeDTO>> getRevenueOverTime(
            @Valid
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<RevenueOverTimeDTO> revenueOverTime = orderService.getRevenueOverTime(from, to);
        return ResponseEntity.ok(revenueOverTime);
    }

    @GetMapping("/reports/best-seller-products")
    public List<BestSellerProductDTO> getBestSellerProducts(
            @Valid
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return orderDetailService.getBestSellerProducts(from, to);
    }

    @GetMapping("/reports/revenue-by-category")
    public ResponseEntity<List<RevenueByCategoryDTO>> getRevenueByCategory() {
        List<RevenueByCategoryDTO> revenueList = orderDetailService.getRevenueByCategory();
        return ResponseEntity.ok(revenueList);
    }

    @GetMapping("/reports/top-spending-customers")
    public List<TopSpendingCustomerDTO> getTopSpendingCustomers(
            @Valid
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return orderService.getTopSpendingCustomers(from, to);
    }

    @GetMapping("/new-accounts-this-month")
    public List<User> getNewAccountsThisMonth() {
        return adminService.getNewAccountsThisMonth();
    }

    @GetMapping("/invoices-over-time")
    public Map<String, Object> getInvoiceCountOverTime(
            @Valid
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        long count = orderService.getInvoiceCountOverTime(from, to);
        return Map.of("from", from, "to", to, "totalInvoices", count);
    }
}