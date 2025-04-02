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

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final AuthService userService;
    private final OrderService orderService;

    @GetMapping("/account")
    public ResponseEntity<UserResponseDTO> getUserAccount(@Valid @RequestParam Long userId) {
        UserResponseDTO user = userService.getUserAccount(userId);
        return ResponseEntity.ok(user);
    }
    @PutMapping("/account")
    public ResponseEntity<UserResponseDTO> updateUserAccount(@Valid @RequestParam Long userId,
                                                             @RequestBody UserUpdateRequestDTO requestDTO) {
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
    public ResponseEntity<List<OrderHistoryResponseDTO>> getOrdersByStatus(@Valid @PathVariable OrderStatus orderStatus) {
        List<OrderHistoryResponseDTO> orders = orderService.getOrdersByStatus(orderStatus);
        return ResponseEntity.ok(orders);
    }
    @PutMapping("/history/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@Valid @PathVariable Long orderId) throws CustomerException{
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Order has been canceled successfully.");
    }



}
