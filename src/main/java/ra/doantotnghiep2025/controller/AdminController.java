    package ra.doantotnghiep2025.controller;

    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.data.domain.Page;
    import org.springframework.http.ResponseEntity;
    import ra.doantotnghiep2025.exception.CustomerException;
    import ra.doantotnghiep2025.model.dto.*;
    import ra.doantotnghiep2025.service.AdminService;

    import java.util.List;

    @RestController
    @RequestMapping("/api/v1/admin")
    public class AdminController {
        @Autowired
        private AdminService adminService;
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

    }
