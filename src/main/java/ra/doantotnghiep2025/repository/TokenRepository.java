package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.doantotnghiep2025.model.entity.TokenBlackList;

public interface TokenRepository extends JpaRepository<TokenBlackList,Long> {
    Boolean existsByToken(String token);
}