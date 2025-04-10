package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.OrderDetailResponseDTO;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartRequestDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartResponseDTO;
import ra.doantotnghiep2025.model.entity.*;
import ra.doantotnghiep2025.repository.*;
import ra.doantotnghiep2025.service.ShoppingCartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ShoppingCartServiceImp implements ShoppingCartService {
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public List<ShoppingCartResponseDTO> getShoppingCartItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        return shoppingCartRepository.findByUser(user).stream().map(cart -> ShoppingCartResponseDTO.builder()
                .shoppingCartId(cart.getShoppingCartId())
                .productId(cart.getProduct().getProductId())
                .productName(cart.getProduct().getProductName())
                .unitPrice(BigDecimal.valueOf(cart.getProduct().getProductPrice()))
                .currency("VND") // Giả sử giá sản phẩm là VND
                .orderQuantity(cart.getOrderQuantity())
                .image(cart.getProduct().getProductImage())
                .build()).collect(Collectors.toList());
    }

    @Override
    public ShoppingCartResponseDTO addToCart(Long userId, ShoppingCartRequestDTO requestDTO) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        Products product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new CustomerException("Sản phẩm không tồn tại"));

        ShoppingCart cart = shoppingCartRepository.findByUserAndProduct(user, product)
                .orElse(ShoppingCart.builder()
                        .user(user)
                        .product(product)
                        .orderQuantity(0)
                        .build());

        cart.setOrderQuantity(cart.getOrderQuantity() + requestDTO.getQuantity());
        ShoppingCart savedCart = shoppingCartRepository.save(cart);

        return ShoppingCartResponseDTO.builder()
                .shoppingCartId(savedCart.getShoppingCartId())
                .productId(savedCart.getProduct().getProductId())
                .productName(savedCart.getProduct().getProductName())
                .unitPrice(BigDecimal.valueOf(savedCart.getProduct().getProductPrice()))
                .currency("VND") // Giả sử giá sản phẩm là VND
                .orderQuantity(savedCart.getOrderQuantity())
                .image(savedCart.getProduct().getProductImage())
                .build();
    }

    @Override
    public ShoppingCartResponseDTO updateCartItem(Long cartItemId, int quantity) throws CustomerException {
        ShoppingCart cart = shoppingCartRepository.findById(cartItemId)
                .orElseThrow(() -> new CustomerException("Sản phẩm trong giỏ hàng không tồn tại"));

        cart.setOrderQuantity(quantity);
        ShoppingCart updatedCart = shoppingCartRepository.save(cart);

        return ShoppingCartResponseDTO.builder()
                .shoppingCartId(updatedCart.getShoppingCartId())
                .productId(updatedCart.getProduct().getProductId())
                .productName(updatedCart.getProduct().getProductName())
                .unitPrice(BigDecimal.valueOf(updatedCart.getProduct().getProductPrice()))
                .currency("VND") // Giả sử giá sản phẩm là VND
                .orderQuantity(updatedCart.getOrderQuantity())
                .image(updatedCart.getProduct().getProductImage())
                .build();
    }

    @Override
    public void removeCartItem(Long userId, Long cartItemId) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        ShoppingCart cartItem = shoppingCartRepository.findByShoppingCartIdAndUser(cartItemId, user)
                .orElseThrow(() -> new CustomerException("Sản phẩm không tồn tại trong giỏ hàng"));

        shoppingCartRepository.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        shoppingCartRepository.deleteAllByUser(user);
    }

    @Override
    @Transactional
    public OrderResponseDTO checkout(Long userId, String receiveAddress, String receiveName, String receivePhone, String note) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng trống!");
        }

        // Kiểm tra số lượng tồn kho trước khi xử lý
        for (ShoppingCart cartItem : cartItems) {
            Products product = cartItem.getProduct();
            if (product.getProductQuantity() < cartItem.getOrderQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sản phẩm " + product.getProductName() + " không đủ số lượng!");
            }
        }

        String serialNumber = UUID.randomUUID().toString();
        if (note == null || note.trim().isEmpty()) {
            note = "Không có ghi chú";
        }

        Order order = Order.builder()
                .user(user)
                .serialNumber(serialNumber)
                .createdAt(LocalDateTime.now())
                .receivedAt(LocalDateTime.now().plusDays(4))
                .status(OrderStatus.WAITING)
                .totalPrice(BigDecimal.ZERO)
                .receiveAddress(receiveAddress)
                .receiveName(receiveName)
                .receivePhone(receivePhone)
                .note(note)
                .build();
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        Set<OrderDetailResponseDTO> orderDetailList = new HashSet<>();

        for (ShoppingCart cartItem : cartItems) {
            Products product = cartItem.getProduct();
            // Giảm số lượng tồn kho
            product.setProductQuantity(product.getProductQuantity() - cartItem.getOrderQuantity());
            // Tăng số lượng đã bán
            product.setSoldQuantity(product.getSoldQuantity() + cartItem.getOrderQuantity());
            productRepository.save(product); // Lưu thay đổi cả productQuantity và soldQuantity

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .name(product.getProductName())
                    .orderQuantity(cartItem.getOrderQuantity())
                    .unitPrice(BigDecimal.valueOf(product.getProductPrice()))
                    .build();
            orderDetailRepository.save(orderDetail);

            total = total.add(BigDecimal.valueOf(product.getProductPrice())
                    .multiply(BigDecimal.valueOf(cartItem.getOrderQuantity())));

            orderDetailList.add(OrderDetailResponseDTO.builder()
                    .id(orderDetail.getId())
                    .name(orderDetail.getName())
                    .unitPrice(orderDetail.getUnitPrice())
                    .orderQuantity(orderDetail.getOrderQuantity())
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .build());
        }

        order.setTotalPrice(total);
        orderRepository.save(order);

        shoppingCartRepository.deleteAll(cartItems);

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .serialNumber(order.getSerialNumber())
                .userId(order.getUser().getId())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .receiveAddress(order.getReceiveAddress())
                .receiveName(order.getReceiveName())
                .receivePhone(order.getReceivePhone())
                .note(order.getNote())
                .build();
    }
}