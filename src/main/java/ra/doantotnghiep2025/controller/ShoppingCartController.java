package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartRequestDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartResponseDTO;
import ra.doantotnghiep2025.service.ShoppingCartService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/cart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public ResponseEntity<List<ShoppingCartResponseDTO>> getCartItems(@Valid @RequestParam Long userId) {
        return ResponseEntity.ok(shoppingCartService.getShoppingCartItems(userId));
    }
    @PostMapping("/add")
    public ResponseEntity<ShoppingCartResponseDTO> addToCart (
            @Valid
            @RequestParam Long userId,
            @Validated @RequestBody ShoppingCartRequestDTO requestDTO) throws CustomerException {
        return ResponseEntity.ok(shoppingCartService.addToCart(userId, requestDTO));
    }
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> updateCartItem(
            @Valid
            @PathVariable Long cartItemId,
            @RequestBody ShoppingCartRequestDTO requestDTO) throws CustomerException{

        ShoppingCartResponseDTO response = shoppingCartService.updateCartItem(cartItemId, requestDTO.getQuantity());
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<?> removeCartItem(@Valid @RequestHeader("userId") Long userId,
                                            @PathVariable Long cartItemId) {
        try {
            shoppingCartService.removeCartItem(userId, cartItemId);
            return ResponseEntity.ok("Sản phẩm đã được xóa khỏi giỏ hàng.");
        } catch (CustomerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@Valid @RequestHeader("userId") Long userId) {
        try {
            shoppingCartService.clearCart(userId);
            return ResponseEntity.ok("Giỏ hàng đã được xóa thành công.");
        } catch (CustomerException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @Valid
            @RequestParam Long userId,
            @RequestParam String receiveAddress,
            @RequestParam String receiveName,
            @RequestParam String receivePhone,
            @RequestParam(required = false) String note) {

        OrderResponseDTO order = shoppingCartService.checkout(userId, receiveAddress, receiveName, receivePhone, note);
        return ResponseEntity.ok(order);
    }



}
