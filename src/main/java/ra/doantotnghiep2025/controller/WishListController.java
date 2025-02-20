package ra.doantotnghiep2025.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.WishListRequestDTO;
import ra.doantotnghiep2025.model.dto.WishListResponseDTO;
import ra.doantotnghiep2025.service.WishListService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user/wish-list")
@RequiredArgsConstructor
public class WishListController {
    private final WishListService wishListService;

    @PostMapping
    public ResponseEntity<String> addToWishList(@RequestParam Long userId, @RequestBody WishListRequestDTO request) throws CustomerException {
        wishListService.addToWishList(userId, request);
        return ResponseEntity.ok("Product added to wishlist successfully.");
    }
    @GetMapping
    public ResponseEntity<List<WishListResponseDTO>> getWishList(@RequestParam Long userId) throws CustomerException{
        List<WishListResponseDTO> wishList = wishListService.getWishList(userId);
        return ResponseEntity.ok(wishList);
    }
    @DeleteMapping("/{wishListId}")
    public ResponseEntity<String> removeFromWishList(@PathVariable Long wishListId) throws CustomerException{
        wishListService.removeFromWishList(wishListId);
        return ResponseEntity.ok("Product removed from wishlist successfully.");
    }
}
