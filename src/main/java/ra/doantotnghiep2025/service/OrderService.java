// File: src/main/java/ra/doantotnghiep2025/service/OrderService.java
package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.*;
import ra.doantotnghiep2025.model.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Page<OrderResponseDTO> getAllOrders(Pageable pageable);
    Page<OrderResponseDTO> getOrdersByStatus(OrderStatus status, Pageable pageable);
    OrderResponseDTO getOrderById(Long orderId) throws CustomerException;
    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus orderStatus) throws CustomerException;
    BigDecimal getSalesRevenueOverTime(LocalDateTime from, LocalDateTime to);
    List<TopSpendingCustomerDTO> getTopSpendingCustomers(LocalDateTime from, LocalDateTime to);
    long getInvoiceCountOverTime(LocalDateTime from, LocalDateTime to);
    List<OrderHistoryResponseDTO> getOrderHistory(Long userId);
    OrderResponseDTO getOrderBySerialNumber(String serialNumber);
    List<OrderHistoryResponseDTO> getOrdersByStatus(OrderStatus status, Long userId);
    void cancelOrder(Long orderId) throws CustomerException;
    List<RevenueOverTimeDTO> getRevenueOverTime(LocalDateTime from, LocalDateTime to);
}