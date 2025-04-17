// File: src/main/java/ra/doantotnghiep2025/service/imp/OrderServiceImp.java
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    public OrderResponseDTO getOrderById(Long orderId) throws CustomerException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy đơn hàng với ID: " + orderId));
        return mapToOrderResponseDTO(order);
    }

    @Override
    public OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus orderStatus) throws CustomerException {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (orderStatus == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy đơn hàng với ID: " + orderId));

        validateOrderStatusTransition(order.getStatus(), orderStatus);

        order.setStatus(orderStatus);
        orderRepository.save(order);

        return mapToOrderResponseDTO(order);
    }

    private void validateOrderStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) throws CustomerException {
        if (currentStatus == null || newStatus == null) {
            throw new CustomerException("Trạng thái hiện tại hoặc trạng thái mới không hợp lệ");
        }

        switch (currentStatus) {
            case WAITING:
                if (newStatus != OrderStatus.CONFIRM && newStatus != OrderStatus.CANCEL) {
                    throw new CustomerException("Đơn hàng ở trạng thái WAITING chỉ có thể chuyển sang CONFIRM hoặc CANCEL");
                }
                break;
            case CONFIRM:
                if (newStatus != OrderStatus.DELIVERY && newStatus != OrderStatus.CANCEL) {
                    throw new CustomerException("Đơn hàng ở trạng thái CONFIRM chỉ có thể chuyển sang DELIVERY hoặc CANCEL");
                }
                break;
            case DELIVERY:
                if (newStatus != OrderStatus.SUCCESS && newStatus != OrderStatus.CANCEL) {
                    throw new CustomerException("Đơn hàng ở trạng thái DELIVERY chỉ có thể chuyển sang SUCCESS hoặc CANCEL");
                }
                break;
            case SUCCESS:
            case CANCEL:
                throw new CustomerException("Đơn hàng đã ở trạng thái " + currentStatus + ", không thể thay đổi trạng thái");
            default:
                throw new CustomerException("Trạng thái hiện tại không hợp lệ: " + currentStatus);
        }
    }

    @Override
    public BigDecimal getSalesRevenueOverTime(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByCreatedAtBetween(from, to)
                .stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<RevenueOverTimeDTO> getRevenueOverTime(LocalDateTime from, LocalDateTime to) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(from, to);

        Map<LocalDate, BigDecimal> revenueByDate = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)
                ));

        return revenueByDate.entrySet().stream()
                .map(entry -> RevenueOverTimeDTO.builder()
                        .date(entry.getKey())
                        .totalRevenue(entry.getValue())
                        .build())
                .sorted(Comparator.comparing(RevenueOverTimeDTO::getDate))
                .collect(Collectors.toList());
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
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public long getInvoiceCountOverTime(LocalDateTime from, LocalDateTime to) {
        return orderRepository.countByCreatedAtBetween(from, to);
    }

    @Override
    public List<OrderHistoryResponseDTO> getOrderHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        List<Order> orders = orderRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        System.out.println("Đã tìm thấy đơn hàng cho userId " + userId + ": " + orders.size());

        return orders.stream().map(this::mapToOrderHistoryResponseDTO).collect(Collectors.toList());
    }

    @Override
    public OrderResponseDTO getOrderBySerialNumber(String serialNumber) {
        Order order = orderRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new RuntimeException("Order not found with serial number: " + serialNumber));

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .serialNumber(order.getSerialNumber())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .note(order.getNote())
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .build();
    }

    @Override
    public List<OrderHistoryResponseDTO> getOrdersByStatus(OrderStatus status, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
        List<Order> orders = orderRepository.findByUserAndStatus(user, status);
        return orders.stream().map(this::mapToOrderHistoryResponseDTO).collect(Collectors.toList());
    }

    @Override
    public void cancelOrder(Long orderId) throws CustomerException {
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
                .id(orderDetail.getId())
                .name(orderDetail.getProduct().getProductName())
                .unitPrice(orderDetail.getUnitPrice())
                .orderQuantity(orderDetail.getOrderQuantity())
                .productId(orderDetail.getProduct().getProductId())
                .productName(orderDetail.getProduct().getProductName())
                .build();
    }

    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
        List<OrderDetailResponseDTO> items = order.getOrderDetails().stream()
                .map(detail -> OrderDetailResponseDTO.builder()
                        .id(detail.getId())
                        .name(detail.getName())
                        .unitPrice(detail.getUnitPrice())
                        .orderQuantity(detail.getOrderQuantity())
                        .productId(detail.getProduct().getProductId())
                        .productName(detail.getProduct().getProductName()) // Ánh xạ productName
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .orderId(order.getOrderId())
                .serialNumber(order.getSerialNumber())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .note(order.getNote() != null ? order.getNote() : "")
                .receiveName(order.getReceiveName())
                .receiveAddress(order.getReceiveAddress())
                .receivePhone(order.getReceivePhone().trim())
                .createdAt(order.getCreatedAt())
                .receivedAt(order.getReceivedAt())
                .items(items) // Gắn danh sách chi tiết đơn hàng
                .build();
    }
}