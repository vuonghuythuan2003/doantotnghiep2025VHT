// File 3: ShoppingCartController.java
package ra.doantotnghiep2025.controller;

import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartRequestDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartResponseDTO;
import ra.doantotnghiep2025.service.PayPalService;
import ra.doantotnghiep2025.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
    public ResponseEntity<Map<String, String>> checkout(
            @Valid @RequestParam Long userId,
            @RequestParam String receiveAddress,
            @RequestParam String receiveName,
            @RequestParam String receivePhone,
            @RequestParam(required = false) String note) {
        Map<String, String> response = new HashMap<>();

        // Validate receivePhone
        if (receivePhone == null || receivePhone.length() > 15) {
            log.warn("Invalid receivePhone: {}", receivePhone);
            response.put("message", "Số điện thoại không hợp lệ! Độ dài tối đa là 15 ký tự.");
            return ResponseEntity.badRequest().body(response);
        }
        if (!receivePhone.matches("\\d{10,11}")) {
            log.warn("Invalid receivePhone format: {}", receivePhone);
            response.put("message", "Số điện thoại phải có 10-11 chữ số!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<ShoppingCartResponseDTO> cartItems = shoppingCartService.getShoppingCartItems(userId);
            if (cartItems == null || cartItems.isEmpty()) {
                log.warn("Cart is empty for userId: {}", userId);
                response.put("message", "Giỏ hàng trống, không thể thanh toán!");
                return ResponseEntity.badRequest().body(response);
            }

            double total = cartItems.stream()
                    .mapToDouble(item -> {
                        double price = (item.getUnitPrice() == null ? 0.0 : item.getUnitPrice().doubleValue());
                        int quantity = item.getOrderQuantity();
                        log.info("Item: {}, Price: {}, Quantity: {}", item.getProductName(), price, quantity);
                        return price * quantity;
                    })
                    .sum();

            log.info("Calculated total for userId {}: {}", userId, total);
            if (total <= 0) {
                log.warn("Invalid total (<= 0) for userId: {}", userId);
                response.put("message", "Tổng tiền không hợp lệ! Tổng tiền phải lớn hơn 0.");
                return ResponseEntity.badRequest().body(response);
            }

            // Sửa cách xây dựng successUrl
            StringBuilder successUrl = new StringBuilder("http://localhost:5173/user/cart/checkout/success");
            successUrl.append("?userId=").append(userId)
                    .append("&receiveAddress=").append(encodeURIComponent(receiveAddress))
                    .append("&receiveName=").append(encodeURIComponent(receiveName))
                    .append("&receivePhone=").append(encodeURIComponent(receivePhone));
            if (note != null) {
                successUrl.append("&note=").append(encodeURIComponent(note));
            }
            String cancelUrl = "http://localhost:5173/user/cart/checkout/cancel";

            String approvalUrl = payPalService.createPayment(
                    total,
                    "VND",
                    "Thanh toán đơn hàng cho user " + userId,
                    cancelUrl,
                    successUrl.toString(),
                    true
            );

            response.put("redirectUrl", approvalUrl);
            return ResponseEntity.ok(response);
        } catch (PayPalRESTException e) {
            log.error("Error creating PayPal payment: {}", e.getMessage(), e);
            response.put("message", "Lỗi khi tạo thanh toán PayPal: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
        log.info("Received callback from PayPal for paymentId: {}, payerId: {}", paymentId, payerId);
        log.info("Received receivePhone: {}", receivePhone);
        log.info("Received note: {}", note);

        // Validate receivePhone
        if (receivePhone == null || receivePhone.length() > 15) {
            log.warn("Invalid receivePhone: {}", receivePhone);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Số điện thoại không hợp lệ! Độ dài tối đa là 15 ký tự.");
            return ResponseEntity.badRequest().body(response);
        }
        if (!receivePhone.matches("\\d{10,11}")) {
            log.warn("Invalid receivePhone format: {}", receivePhone);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Số điện thoại phải có 10-11 chữ số!");
            return ResponseEntity.badRequest().body(response);
        }

        Map<String, Object> response = new HashMap<>();
        try {
            Payment payment = payPalService.executePayment(paymentId, payerId);
            log.info("Payment state: {}", payment.getState());
            if (payment.getState().equals("approved")) {
                OrderResponseDTO order = shoppingCartService.checkout(userId, receiveAddress, receiveName, receivePhone, note);
                log.info("Order created with ID: {}, cart cleared", order.getOrderId());
                response.put("message", "Thanh toán thành công! Mã đơn hàng: " + order.getOrderId());
                response.put("cartCleared", true);
                response.put("redirect", "/user?orderId=" + order.getOrderId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Payment not approved, state: {}", payment.getState());
                response.put("message", "Thanh toán không được phê duyệt! Trạng thái: " + payment.getState());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (PayPalRESTException e) {
            log.error("Error executing PayPal payment: {}", e.getMessage(), e);
            response.put("message", "Lỗi khi xác nhận thanh toán: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/checkout/cancel")
    public ResponseEntity<Map<String, String>> checkoutCancel() {
        log.info("PayPal payment cancelled");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Thanh toán đã bị hủy!");
        return ResponseEntity.ok(response);
    }

    private String encodeURIComponent(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            log.error("Error encoding URI component: {}", value, e);
            return value;
        }
    }
    @PostMapping("/checkout/cod")
    public ResponseEntity<Map<String, String>> checkoutCOD(
            @Valid @RequestParam Long userId,
            @RequestParam String receiveAddress,
            @RequestParam String receiveName,
            @RequestParam String receivePhone,
            @RequestParam(required = false) String note) {
        Map<String, String> response = new HashMap<>();

        // Validate receivePhone
        if (receivePhone == null || receivePhone.length() > 15) {
            log.warn("Invalid receivePhone: {}", receivePhone);
            response.put("message", "Số điện thoại không hợp lệ! Độ dài tối đa là 15 ký tự.");
            return ResponseEntity.badRequest().body(response);
        }
        if (!receivePhone.matches("\\d{10,11}")) {
            log.warn("Invalid receivePhone format: {}", receivePhone);
            response.put("message", "Số điện thoại phải có 10-11 chữ số!");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            List<ShoppingCartResponseDTO> cartItems = shoppingCartService.getShoppingCartItems(userId);
            if (cartItems == null || cartItems.isEmpty()) {
                log.warn("Cart is empty for userId: {}", userId);
                response.put("message", "Giỏ hàng trống, không thể thanh toán!");
                return ResponseEntity.badRequest().body(response);
            }

            double total = cartItems.stream()
                    .mapToDouble(item -> {
                        double price = (item.getUnitPrice() == null ? 0.0 : item.getUnitPrice().doubleValue());
                        int quantity = item.getOrderQuantity();
                        log.info("Item: {}, Price: {}, Quantity: {}", item.getProductName(), price, quantity);
                        return price * quantity;
                    })
                    .sum();

            log.info("Calculated total for userId {}: {}", userId, total);
            if (total <= 0) {
                log.warn("Invalid total (<= 0) for userId: {}", userId);
                response.put("message", "Tổng tiền không hợp lệ! Tổng tiền phải lớn hơn 0.");
                return ResponseEntity.badRequest().body(response);
            }

            // Tạo đơn hàng với phương thức thanh toán COD
            OrderResponseDTO order = shoppingCartService.checkout(userId, receiveAddress, receiveName, receivePhone, note);
            log.info("Order created with ID: {}, cart cleared", order.getOrderId());
            response.put("orderId", order.getOrderId().toString());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing COD checkout: {}", e.getMessage(), e);
            response.put("message", "Lỗi khi xử lý thanh toán COD: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}