package ra.doantotnghiep2025.service;

import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.OrderResponseDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartRequestDTO;
import ra.doantotnghiep2025.model.dto.ShoppingCartResponseDTO;
import ra.doantotnghiep2025.model.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCartResponseDTO> getShoppingCartItems(Long userId);
    ShoppingCartResponseDTO addToCart(Long userId, ShoppingCartRequestDTO requestDTO) throws CustomerException;
    ShoppingCartResponseDTO updateCartItem(Long cartItemId, int quantity) throws CustomerException;
    void removeCartItem(Long userId, Long cartItemId) throws CustomerException;
    void clearCart(Long userId) throws CustomerException;
    OrderResponseDTO checkout(Long userId, String receiveAddress, String receiveName, String receivePhone, String note);
}