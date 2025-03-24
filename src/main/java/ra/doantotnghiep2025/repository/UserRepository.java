package ra.doantotnghiep2025.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.model.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
    boolean existsByUsername(String username);
    Page<User> findAll(Pageable pageable);
    List<User> findByFullnameContainingIgnoreCase(String fullName);
    List<User> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);

}
