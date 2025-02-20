package ra.doantotnghiep2025.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ra.doantotnghiep2025.model.entity.WishList;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.model.entity.Products;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {
    Optional<WishList> findByUserAndProduct(User user, Products product);
    List<WishList> findByUser(User user);

}
