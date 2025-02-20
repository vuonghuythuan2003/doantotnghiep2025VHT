package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.BestSellerProductDTO;
import ra.doantotnghiep2025.model.dto.OrderDetailResponseDTO;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.dto.TopSpendingCustomerDTO;
import ra.doantotnghiep2025.model.entity.Order;
import ra.doantotnghiep2025.model.entity.OrderStatus;
import ra.doantotnghiep2025.repository.OrderRepository;
import ra.doantotnghiep2025.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {
    @Autowired
    private OrderRepository orderRepository;

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



    private OrderResponseDTO mapToOrderResponseDTO(Order order) {
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
                .build();
    }

}
