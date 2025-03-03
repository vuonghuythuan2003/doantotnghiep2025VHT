package ra.doantotnghiep2025.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.WishListRequestDTO;
import ra.doantotnghiep2025.model.dto.WishListResponseDTO;
import ra.doantotnghiep2025.model.entity.Products;
import ra.doantotnghiep2025.model.entity.User;
import ra.doantotnghiep2025.model.entity.WishList;
import ra.doantotnghiep2025.repository.ProductRepository;
import ra.doantotnghiep2025.repository.UserRepository;
import ra.doantotnghiep2025.repository.WishListRepository;
import ra.doantotnghiep2025.service.WishListService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishListServiceImp implements WishListService {
    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void addToWishList(Long userId, WishListRequestDTO request) throws CustomerException{
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy người dùng"));

        Products product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new CustomerException("Không tìm thấy sản phẩm"));

        boolean exists = wishListRepository.findByUserAndProduct(user, product).isPresent();
        if (exists) {
            throw new CustomerException("Sản phẩm đã có trong danh sách yêu thích");
        }

        WishList wishList = WishList.builder()
                .user(user)
                .product(product)
                .build();

        wishListRepository.save(wishList);
    }
    @Override
    public List<WishListResponseDTO> getWishList(Long userId) throws CustomerException{
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomerException("User not found"));

        List<WishList> wishLists = wishListRepository.findByUser(user);

        return wishLists.stream().map(wishList -> {
            Products product = wishList.getProduct();
            return WishListResponseDTO.builder()
                    .wishListId(wishList.getWishListId())
                    .productId(product.getProductId())
                    .productName(product.getProductName())
                    .productImage(product.getProductImage())
                    .price(product.getProductPrice())
                    .build();
        }).collect(Collectors.toList());
    }
    @Override
    public void removeFromWishList(Long wishListId) throws CustomerException{
        WishList wishList = wishListRepository.findById(wishListId)
                .orElseThrow(() -> new CustomerException("Không tìm thấy mục WishList"));

        wishListRepository.delete(wishList);
    }
}
