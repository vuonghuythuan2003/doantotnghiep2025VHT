    package ra.doantotnghiep2025.controller;

    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.Pageable;
    import org.springframework.format.annotation.DateTimeFormat;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.data.domain.Page;
    import org.springframework.http.ResponseEntity;
    import ra.doantotnghiep2025.exception.CustomerException;
    import ra.doantotnghiep2025.model.dto.*;
    import ra.doantotnghiep2025.model.entity.OrderStatus;
    import ra.doantotnghiep2025.model.entity.User;
    import ra.doantotnghiep2025.service.*;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
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

        @GetMapping("/users")
        public ResponseEntity<Page<UserResponseDTO>> getUsers(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(defaultValue = "username") String sortBy,
                @RequestParam(defaultValue = "asc") String direction
        ) {
            return ResponseEntity.ok(adminService.getUsers(page, size, sortBy, direction));
        }

        @PatchMapping("/users/{userId}/role/{roleId}")
        public ResponseEntity<?> updatePermissionUser(@PathVariable Long userId, @PathVariable Long roleId) throws Exception {
            UserRegisterResponseDTO userRegisterResponseDTO = adminService.updatePermission(userId, roleId);
            return ResponseEntity.ok(userRegisterResponseDTO);
        }
        @DeleteMapping("/users/{userId}/role/{roleId}")
        public ResponseEntity<?> deletePermissionUser(@PathVariable Long userId, @PathVariable Long roleId) throws Exception {
            adminService.deleteUserRole(userId, roleId);
            return ResponseEntity.ok("Xóa quyền người dùng thành công");
        }
        @GetMapping("/roles")
        public ResponseEntity<List<RoleResponseDTO>> getRoles(){
            List<RoleResponseDTO> roleRespon = adminService.getRole();
            return ResponseEntity.ok(roleRespon);
        }
        @GetMapping("/users/search")
        public ResponseEntity<List<UserResponseDTO>> searchUsers(@RequestParam String username) {
            List<UserResponseDTO> userRespon = adminService.searchByUserName(username);
            return ResponseEntity.ok(userRespon);
        }
        @GetMapping("products")
        public ResponseEntity<Page<ProductReponseDTO>> getProducts(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(defaultValue = "productName") String sortBy,
                @RequestParam(defaultValue = "asc") String direction
        ) {
            return ResponseEntity.ok(adminService.getProducts(page, size, sortBy, direction));
        }
        @GetMapping("/products/{productId}")
        public ResponseEntity<ProductReponseDTO> getProduct(@PathVariable Long productId) throws CustomerException {
            ProductReponseDTO product = adminService.getProductById(productId);
            return ResponseEntity.ok(product);
        }
        @PostMapping("/products")
        public ResponseEntity<ProductReponseDTO> createProduct(@ModelAttribute @Valid ProductRequestDTO productRequestDTO) throws CustomerException {
            ProductReponseDTO newProduct = adminService.saveProduct(productRequestDTO);
            return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
        }

        @PutMapping("/products/{productId}")
        public ResponseEntity<ProductReponseDTO> updateProduct(@PathVariable Long productId, @ModelAttribute @Valid ProductUpdateDTO productRequestDTO) throws CustomerException {
            ProductReponseDTO updatedProduct = adminService.updateProductById(productId, productRequestDTO);
            return ResponseEntity.ok(updatedProduct);
        }

        @DeleteMapping("/products/{productId}")
        public ResponseEntity<?> deleteProduct(@PathVariable Long productId) throws CustomerException {
            adminService.deleteProductById(productId);
            return ResponseEntity.ok("Xóa sản phẩm thành công");
        }
        @GetMapping("/categories")
        public ResponseEntity<Page<CategoryResponseDTO>> getCategories(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size,
                @RequestParam(defaultValue = "categoryName") String sortBy,
                @RequestParam(defaultValue = "asc") String direction) {

            Page<CategoryResponseDTO> categories = adminService.getCategories(page, size, sortBy, direction);
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
        public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) throws CustomerException {
            boolean isDeleted = adminService.deleteCategoryById(categoryId);
            if (isDeleted) {
                return ResponseEntity.ok("Danh mục đã được xóa thành công!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không thể xóa danh mục.");
            }
        }

        @GetMapping("/orders")
        public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(Pageable pageable) {
            Page<OrderResponseDTO> orders = orderService.getAllOrders(pageable);
            return ResponseEntity.ok(orders);
        }
        @GetMapping("/orders/status/{orderStatus}")
        public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(
                @PathVariable OrderStatus orderStatus, Pageable pageable) {
            Page<OrderResponseDTO> orders = orderService.getOrdersByStatus(orderStatus, pageable);
            return ResponseEntity.ok(orders);
        }

        @GetMapping("/orders/{orderId}")
        public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId) throws CustomerException{
            OrderResponseDTO orderResponse = orderService.getOrderById(orderId);
            return ResponseEntity.ok(orderResponse);
        }
        @PutMapping("/orders/{orderId}/status")
        public ResponseEntity<OrderResponseDTO> updateOrderStatus(
                @PathVariable Long orderId,
                @RequestBody OrderStatus orderStatus) throws CustomerException{
            OrderResponseDTO updatedOrder = orderService.updateOrderStatus(orderId, orderStatus);
            return ResponseEntity.ok(updatedOrder);
        }
        @GetMapping("/sales-revenue-over-time")
        public ResponseEntity<BigDecimal> getSalesRevenueOverTime(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
            return ResponseEntity.ok(orderService.getSalesRevenueOverTime(from, to));
        }
        @GetMapping("/reports/best-seller-products")
        public List<BestSellerProductDTO> getBestSellerProducts(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
        ) {
            return orderDetailService.getBestSellerProducts(from, to);
        }
        @GetMapping("/reports/most-liked-products")
        public List<MostLikedProductDTO> getMostLikedProducts(
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
        ) {
            return productService.getMostLikedProducts(from, to);
        }
        @GetMapping("/reports/revenue-by-category")
        public ResponseEntity<List<RevenueByCategoryDTO>> getRevenueByCategory() {
            List<RevenueByCategoryDTO> revenueList = orderDetailService.getRevenueByCategory();
            return ResponseEntity.ok(revenueList);
        }
        @GetMapping("/reports/top-spending-customers")
        public List<TopSpendingCustomerDTO> getTopSpendingCustomers(
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
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

            long count = orderService.getInvoiceCountOverTime(from, to);
            return Map.of("from", from, "to", to, "totalInvoices", count);
        }

    }
