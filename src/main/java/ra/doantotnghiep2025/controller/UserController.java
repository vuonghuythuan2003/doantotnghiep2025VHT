package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.OrderStatus;
import ra.doantotnghiep2025.service.AuthService;
import ra.doantotnghiep2025.service.BrandService;
import ra.doantotnghiep2025.service.OrderService;
import ra.doantotnghiep2025.service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final AuthService userService;
    private final OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;
    @GetMapping("/account")
    public ResponseEntity<UserResponseDTO> getUserAccount(@Valid @RequestParam Long userId) {
        UserResponseDTO user = userService.getUserAccount(userId);
        return ResponseEntity.ok(user);
    }
    @PutMapping("/account")
    public ResponseEntity<UserResponseDTO> updateUserAccount(@Valid @RequestParam Long userId,
                                                             @ModelAttribute UserUpdateRequestDTO requestDTO) {
        UserResponseDTO updatedUser = userService.updateUserAccount(userId, requestDTO);
        return ResponseEntity.ok(updatedUser);
    }
    @PutMapping("/account/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestParam Long userId,
                                                 @RequestBody ChangePasswordRequestDTO requestDTO) {
        userService.changePassword(userId, requestDTO);
        return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công!");
    }
    @PostMapping("/account/addresses")
    public ResponseEntity<UserResponseDTO> addAddress(
            @Valid
            @RequestParam Long userId,
            @RequestBody AddressRequestDTO addressRequestDTO) throws CustomerException
    {
        return ResponseEntity.ok(userService.addAddress(userId, addressRequestDTO));
    }
    @DeleteMapping("/account/addresses/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @Valid
            @PathVariable Long addressId,
            @RequestParam Long userId) throws CustomerException{
        userService.deleteAddress(addressId, userId);
        return ResponseEntity.ok("Địa chỉ đã được xoá thành công");
    }
    @GetMapping("/account/addresses")
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses(@Valid @RequestParam Long userId) throws CustomerException{
        List<AddressResponseDTO> addresses = userService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }
    @GetMapping("/account/addresses/{addressId}")
    public ResponseEntity<AddressResponseDTO> getAddressById(@Valid @PathVariable Long addressId) throws CustomerException{
        AddressResponseDTO address = userService.getAddressById(addressId);
        return ResponseEntity.ok(address);
    }
    @GetMapping("/history/getAll")
    public ResponseEntity<List<OrderHistoryResponseDTO>> getOrderHistory(@Valid @RequestParam Long userId) {
        System.out.println("Fetching order history for userId: " + userId); // Debug
        return ResponseEntity.ok(orderService.getOrderHistory(userId));
    }


    @GetMapping("/history")
    public ResponseEntity<OrderResponseDTO> getOrderBySerialNumber(@Valid @RequestParam String serialNumber) {
        return ResponseEntity.ok(orderService.getOrderBySerialNumber(serialNumber));
    }

    @GetMapping("/history/{orderStatus}")
    public ResponseEntity<List<OrderHistoryResponseDTO>> getOrdersByStatus(
            @Valid @PathVariable OrderStatus orderStatus,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername(); // Lấy username từ UserDetails
        Long userId = userService.getUserIdByUsername(username); // Giả sử AuthService có phương thức này
        List<OrderHistoryResponseDTO> orders = orderService.getOrdersByStatus(orderStatus, userId);
        return ResponseEntity.ok(orders);
    }
    @PutMapping("/history/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@Valid @PathVariable Long orderId) throws CustomerException{
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Order has been canceled successfully.");
    }
    @GetMapping("/brands")
    public ResponseEntity<Page<BrandResponseDTO>> getBrands(
            @Valid
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "brandName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(brandService.getBrands(page, size, sortBy, direction));
    }

    @GetMapping("/brands/{brandId}")
    public ResponseEntity<List<ProductReponseDTO>> getProductsByBrand(
            @Valid @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ProductReponseDTO> products = productService.getProductsByBrand(brandId, page, size);
        return ResponseEntity.ok(products);
    }
    @GetMapping("/{brandId}")
    public ResponseEntity<BrandResponseDTO> getBrandById(@PathVariable Long brandId) throws CustomerException {
        BrandResponseDTO brand = brandService.getBrandById(brandId);
        return ResponseEntity.ok(brand);
    }
}