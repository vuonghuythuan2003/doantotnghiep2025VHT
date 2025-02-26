package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.model.entity.ShoppingCart;
import ra.doantotnghiep2025.model.entity.User;

import java.util.List;
import java.util.Optional;
@Transactional
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    List<ShoppingCart> findByUser(User user);
    Optional<ShoppingCart> findByUserAndProduct(User user, Products product);
    Optional<ShoppingCart> findByShoppingCartIdAndUser(Long shoppingCartId, User user);  // Đổi 'id' thành 'shoppingCartId'
    void deleteAllByUser(User user);

}
