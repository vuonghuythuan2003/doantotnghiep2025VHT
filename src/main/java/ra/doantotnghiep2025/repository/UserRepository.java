package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findUserByUsername(String username);
    boolean existsByUsername(String username);
}
