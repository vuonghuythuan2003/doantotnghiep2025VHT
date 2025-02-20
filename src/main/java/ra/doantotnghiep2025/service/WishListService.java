package ra.doantotnghiep2025.service;

import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.WishListRequestDTO;
import ra.doantotnghiep2025.model.dto.WishListResponseDTO;

import java.util.List;

public interface WishListService {
    void addToWishList(Long userId, WishListRequestDTO request) throws CustomerException;
    List<WishListResponseDTO> getWishList(Long userId) throws CustomerException;
    void removeFromWishList(Long wishListId) throws CustomerException;

}
