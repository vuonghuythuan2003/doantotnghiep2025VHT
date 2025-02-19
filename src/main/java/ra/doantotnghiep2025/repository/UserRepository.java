package ra.doantotnghiep2025.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.model.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
    boolean existsByUsername(String username);
    Page<User> findAll(Pageable pageable);
    List<User> findByFullNameContainingIgnoreCase(String fullName);

}
