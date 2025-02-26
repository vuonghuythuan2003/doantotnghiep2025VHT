package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.Order;
import ra.doantotnghiep2025.model.entity.OrderDetail;
import ra.doantotnghiep2025.model.entity.OrderStatus;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.repository.OrderRepository;
import ra.doantotnghiep2025.repository.UserRepository;
import ra.doantotnghiep2025.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToOrderResponseDTO);
    }

    @Override
    public Page<OrderResponseDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToOrderResponseDTO);
    }

    @Override
    public OrderResponseDTO getOrderById(Long orderId) throws CustomerException{
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy đơn hàng với ID: " + orderId));

        return mapToOrderResponseDTO(order);
    }

    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus orderStatus) throws CustomerException{
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy đơn hàng với ID: " + orderId));

        order.setStatus(orderStatus);
        orderRepository.save(order);

        return mapToOrderResponseDTO(order);
    }

    @Override
    public BigDecimal getSalesRevenueOverTime(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByCreatedAtBetween(from, to)
                .stream()
                .map(order -> order.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<TopSpendingCustomerDTO> getTopSpendingCustomers(LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(from, to);

        Map<Long, BigDecimal> customerSpending = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getUser().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)
                ));

        return customerSpending.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(entry -> {
                    Order sampleOrder = orders.stream()
                            .filter(order -> order.getUser().getId().equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);

                    return sampleOrder != null ? TopSpendingCustomerDTO.builder()
                            .userId(sampleOrder.getUser().getId())
                            .fullName(sampleOrder.getUser().getFullname())
                            .email(sampleOrder.getUser().getEmail())
                            .totalSpent(entry.getValue())
                            .build() : null;
                })
                .toList();
    }

    @Override
    public long getInvoiceCountOverTime(LocalDateTime from, LocalDateTime to) {
        return orderRepository.countByCreatedAtBetween(from, to);
    }

    @Override
    public List<OrderHistoryResponseDTO> getOrderHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Order> orders = orderRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        System.out.println("Orders found for userId " + userId + ": " + orders.size()); // Debug

        return orders.stream().map(this::mapToOrderHistoryResponseDTO).collect(Collectors.toList());
    }


    @Override
    public OrderResponseDTO getOrderBySerialNumber(String serialNumber) {
        Order order = orderRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with serial number: " + serialNumber));

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .serialNumber(order.getSerialNumber())
                .userFullName(order.getUser().getFullname())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .note(order.getNote())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .orderDetails(order.getOrderDetails().stream()
                        .map(detail -> OrderDetailResponseDTO.builder()
                                .id(detail.getId())
                                .name(detail.getProduct().getProductName())
                                .unitPrice(detail.getUnitPrice())
                                .orderQuantity(detail.getOrderQuantity())
                                .productId(detail.getProduct().getProductId())
                                .productName(detail.getProduct().getProductName())
                                .build())
                        .collect(Collectors.toSet()))

                .build();
    }

    @Override
    public List<OrderHistoryResponseDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream().map(this::mapToOrderHistoryResponseDTO).collect(Collectors.toList());
    }

    @Override

    public void cancelOrder(Long orderId) throws CustomerException{
        Order order = orderRepository.findByOrderIdAndStatus(orderId, OrderStatus.WAITING)
                .orElseThrow(() -> new CustomerException("Order not found or cannot be canceled"));

        order.setStatus(OrderStatus.CANCEL);
        orderRepository.save(order);
    }

    private OrderHistoryResponseDTO mapToOrderHistoryResponseDTO(Order order) {
        return OrderHistoryResponseDTO.builder()
                .orderId(order.getOrderId())
                .serialNumber(order.getSerialNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .orderDetails(order.getOrderDetails().stream().map(this::mapToOrderDetailResponseDTO).collect(Collectors.toList()))
                .build();
    }

    private OrderDetailResponseDTO mapToOrderDetailResponseDTO(OrderDetail orderDetail) {
        return OrderDetailResponseDTO.builder()
                .id(orderDetail.getId()) // ID của chi tiết đơn hàng
                .name(orderDetail.getProduct().getProductName()) // Tên sản phẩm
                .unitPrice(orderDetail.getUnitPrice()) // Giá từng đơn vị
                .orderQuantity(orderDetail.getOrderQuantity()) // Số lượng đặt
                .productId(orderDetail.getProduct().getProductId()) // ID sản phẩm
                .productName(orderDetail.getProduct().getProductName()) // Tên sản phẩm
                .build();
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .serialNumber(order.getSerialNumber())
                .userFullName(order.getUser().getFullname())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .note(order.getNote() != null ? order.getNote() : "")  // Tránh null
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone().trim())  // Loại bỏ xuống dòng
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .orderDetails(order.getOrderDetails() != null ?
                        order.getOrderDetails().stream()
                                .map(detail -> new OrderDetailResponseDTO(
                                        detail.getId(),
                                        detail.getProduct().getProductName(),
                                        detail.getUnitPrice(),
                                        detail.getOrderQuantity(),
                                        detail.getProduct().getProductId(),
                                        detail.getProduct().getProductName()
                                ))
                                .collect(Collectors.toSet())
                        : Collections.emptySet())
                .build();
    }




}
