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
        // Lấy thông tin người dùng
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"));

        // Lấy danh sách sản phẩm trong giỏ hàng
        List<ShoppingCart> cartItems = shoppingCartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Giỏ hàng trống!");
        }

        // Kiểm tra số lượng sản phẩm có đủ không
        for (ShoppingCart cartItem : cartItems) {
            Products product = cartItem.getProduct();
            if (product.getProductQuantity() < cartItem.getOrderQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Sản phẩm " + product.getProductName() + " không đủ số lượng!");
            }
        }

        // Tạo mã đơn hàng (serialNumber)
        String serialNumber = UUID.randomUUID().toString();

        // Nếu không có ghi chú, đặt giá trị mặc định
        if (note == null || note.trim().isEmpty()) {
            note = "Không có ghi chú";
        }

        // Tạo đơn hàng
        Order order = Order.builder()
                .user(user)
                .serialNumber(serialNumber)
                .createdAt(LocalDateTime.now())
                .receivedAt(LocalDateTime.now().plusDays(4)) // Ngày nhận hàng mặc định +4 ngày
                .status(OrderStatus.WAITING)
                .totalPrice(BigDecimal.ZERO)
                .receiveAddress(receiveAddress)
                .receiveName(receiveName)
                .receivePhone(receivePhone)
                .note(note)  // Gán giá trị ghi chú
                .build();
        orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;
        Set<OrderDetailResponseDTO> orderDetailList = new HashSet<>();

        // Xử lý đơn hàng và cập nhật sản phẩm
        for (ShoppingCart cartItem : cartItems) {
            Products product = cartItem.getProduct();
            product.setProductQuantity(product.getProductQuantity() - cartItem.getOrderQuantity());
            productRepository.save(product);

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

            // Thêm vào danh sách orderDetailList để trả về DTO
            orderDetailList.add(OrderDetailResponseDTO.builder()
                    .id(orderDetail.getId())
                    .name(orderDetail.getName())
                    .unitPrice(orderDetail.getUnitPrice())
                    .orderQuantity(orderDetail.getOrderQuantity())
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .build());
        }

        // Cập nhật tổng tiền đơn hàng
        order.setTotalPrice(total);
        orderRepository.save(order);

        // Xóa giỏ hàng sau khi đặt hàng thành công
        shoppingCartRepository.deleteAll(cartItems);

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .serialNumber(order.getSerialNumber())
                .userFullName(user.getFullname())  // Truyền thông tin user đầy đủ
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .receiveAddress(order.getReceiveAddress())
                .receiveName(order.getReceiveName())
                .receivePhone(order.getReceivePhone())
                .note(order.getNote())  // Ghi chú từ người dùng
                .orderDetails(orderDetailList)  // Thêm danh sách order details
                .build();
    }


}
