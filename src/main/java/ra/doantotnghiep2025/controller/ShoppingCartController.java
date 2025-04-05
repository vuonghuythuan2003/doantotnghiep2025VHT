package ra.doantotnghiep2025.controller;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartRequestDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartResponseDTO;
import ra.doantotnghiep2025.service.PayPalService;
import ra.doantotnghiep2025.service.ShoppingCartService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/v1/user/cart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private PayPalService payPalService;

    @GetMapping("/list")
    public ResponseEntity<List<ShoppingCartResponseDTO>> getCartItems(@Valid @RequestParam Long userId) {
        return ResponseEntity.ok(shoppingCartService.getShoppingCartItems(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<ShoppingCartResponseDTO> addToCart(
            @Valid @RequestParam Long userId,
            @Validated @RequestBody ShoppingCartRequestDTO requestDTO) throws CustomerException {
        return ResponseEntity.ok(shoppingCartService.addToCart(userId, requestDTO));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ShoppingCartResponseDTO> updateCartItem(
            @Valid @PathVariable Long cartItemId,
            @RequestBody ShoppingCartRequestDTO requestDTO) throws CustomerException {
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
    public ResponseEntity<String> checkout(
            @Valid @RequestParam Long userId,
            @RequestParam String receiveAddress,
            @RequestParam String receiveName,
            @RequestParam String receivePhone,
            @RequestParam(required = false) String note) {
        try {
            List<ShoppingCartResponseDTO> cartItems = shoppingCartService.getShoppingCartItems(userId);
            if (cartItems == null || cartItems.isEmpty()) {
                System.out.println("Cart is empty for userId: " + userId);
                return ResponseEntity.badRequest().body("Giỏ hàng trống, không thể thanh toán!");
            }

            double total = cartItems.stream()
                    .mapToDouble(item -> {
                        double price = (item.getUnitPrice() == null ? 0.0 : item.getUnitPrice().doubleValue());
                        int quantity = item.getOrderQuantity();
                        System.out.println("Item: " + item.getProductName() + ", Price: " + price + ", Quantity: " + quantity);
                        return price * quantity;
                    })
                    .sum();

            System.out.println("Calculated total for userId " + userId + ": " + total);
            if (total <= 0) {
                System.out.println("Invalid total (<= 0) for userId: " + userId);
                return ResponseEntity.badRequest().body("Tổng tiền không hợp lệ! Tổng tiền phải lớn hơn 0.");
            }

            Payment payment = payPalService.createPayment(
                    total,
                    "USD",
                    "paypal",
                    "sale",
                    "Thanh toán đơn hàng cho user " + userId,
                    userId,
                    receiveAddress,
                    receiveName,
                    receivePhone,
                    note
            );

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return ResponseEntity.ok("redirect:" + links.getHref());
                }
            }
            return ResponseEntity.status(500).body("Không tìm thấy URL thanh toán!");
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi tạo thanh toán PayPal: " + e.getMessage());
        }
    }

    @GetMapping("/checkout/success")
    public ResponseEntity<Map<String, Object>> checkoutSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam("userId") Long userId,
            @RequestParam("receiveAddress") String receiveAddress,
            @RequestParam("receiveName") String receiveName,
            @RequestParam("receivePhone") String receivePhone,
            @RequestParam(value = "note", required = false) String note) {
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                OrderResponseDTO order = shoppingCartService.checkout(userId, receiveAddress, receiveName, receivePhone, note);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Thanh toán thành công! Mã đơn hàng: " + order.getOrderId());
                response.put("cartCleared", true);
                response.put("redirect", "/user"); // Signal frontend to redirect
                return ResponseEntity.ok(response);
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.singletonMap("message", "Lỗi khi xác nhận thanh toán: " + e.getMessage()));
        }
        return ResponseEntity.status(500).body(Collections.singletonMap("message", "Thanh toán không được phê duyệt!"));
    }

    @GetMapping("/checkout/cancel")
    public ResponseEntity<String> checkoutCancel() {
        return ResponseEntity.ok("Thanh toán đã bị hủy!");
    }
}