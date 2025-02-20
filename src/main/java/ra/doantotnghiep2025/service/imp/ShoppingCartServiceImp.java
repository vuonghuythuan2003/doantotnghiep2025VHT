package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartRequestDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartResponseDTO;
import ra.doantotnghiep2025.model.entity.*;
import ra.doantotnghiep2025.repository.*;
import ra.doantotnghiep2025.service.ShoppingCartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service

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
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return shoppingCartRepository.findByUser(user).stream().map(cart -> ShoppingCartResponseDTO.builder()
                .shoppingCartId(cart.getShoppingCartId())
                .productId(cart.getProduct().getProductId())
                .productName(cart.getProduct().getProductName())
                .unitPrice(BigDecimal.valueOf(cart.getProduct().getProductPrice()))
                .orderQuantity(cart.getOrderQuantity())
                .build()).collect(Collectors.toList());
    }

    @Override

    public ShoppingCartResponseDTO addToCart(Long userId, ShoppingCartRequestDTO requestDTO) throws CustomerException{
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
                .orderQuantity(savedCart.getOrderQuantity())
                .build();
    }

    @Override
    public ShoppingCartResponseDTO updateCartItem(Long cartItemId, int quantity) throws CustomerException{
        // Tìm cart item theo ID
        ShoppingCart cart = shoppingCartRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm trong giỏ hàng không tồn tại"));

        // Cập nhật số lượng
        cart.setOrderQuantity(quantity);
        ShoppingCart updatedCart = shoppingCartRepository.save(cart);

        // Trả về response
        return ShoppingCartResponseDTO.builder()
                .shoppingCartId(updatedCart.getShoppingCartId())
                .productId(updatedCart.getProduct().getProductId())
                .productName(updatedCart.getProduct().getProductName())
                .unitPrice(BigDecimal.valueOf(updatedCart.getProduct().getProductPrice()))
                .orderQuantity(updatedCart.getOrderQuantity())
                .build();
    }

    @Override
    @Transactional
    public void removeCartItem(Long userId, Long cartItemId) throws CustomerException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Người dùng không tồn tại"));

        ShoppingCart cartItem = shoppingCartRepository.findByIdAndUser(cartItemId, user)
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
    public OrderResponseDTO checkout(Long userId) {
        // Lấy danh sách sản phẩm trong giỏ hàng của user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống!");
        }

        // Kiểm tra số lượng sản phẩm có đủ không
        for (ShoppingCart cartItem : cartItems) {
            Products product = cartItem.getProduct();
            if (product.getProductQuantity() < cartItem.getOrderQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getProductName() + " không đủ số lượng!");
            }
        }

        // Tạo đơn hàng
        Order order = Order.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .status(OrderStatus.WAITING) // Đơn hàng đang chờ xử lý
                .totalPrice(BigDecimal.ZERO)
                .build();
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        // Tạo danh sách chi tiết đơn hàng
        for (ShoppingCart cartItem : cartItems) {
            Products product = cartItem.getProduct();

            // Trừ số lượng sản phẩm trong kho
            product.setProductQuantity(product.getProductQuantity() - cartItem.getOrderQuantity());
            productRepository.save(product);

            // Tạo OrderDetail
            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .orderQuantity(cartItem.getOrderQuantity())
                    .unitPrice(BigDecimal.valueOf(product.getProductPrice()))
                    .build();
            orderDetailRepository.save(orderDetail);

            // Cộng tổng tiền đơn hàng
            total = total.add(BigDecimal.valueOf(product.getProductPrice()).multiply(BigDecimal.valueOf(cartItem.getOrderQuantity())));
        }

        // Cập nhật tổng giá trị đơn hàng
        order.setTotalPrice(total);
        orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        shoppingCartRepository.deleteAll(cartItems);

        // Trả về thông tin đơn hàng đã tạo
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .createdAt(order.getCreatedAt())
                .status(OrderStatus.valueOf(order.getStatus().toString()))
                .totalPrice(order.getTotalPrice())
                .build();
    }


}
