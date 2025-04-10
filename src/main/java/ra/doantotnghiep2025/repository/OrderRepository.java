package ra.doantotnghiep2025.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.entity.Order;
import ra.doantotnghiep2025.model.entity.OrderDetail;
import ra.doantotnghiep2025.model.entity.OrderStatus;
import ra.doantotnghiep2025.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Optional<Order> findById(Long orderId);
    //OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus orderStatus);
    List<Order> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<Order> findByUser(User user, Sort sort);
    Optional<Order> findBySerialNumber(String serialNumber);
    Optional<Order> findByOrderIdAndStatus(Long orderId, OrderStatus status);
    List<Order> findByUserAndStatus(User user, OrderStatus status); // Thêm phương thức này
}
