package com.filadelfia.store.filadelfiastore.service.interfaces;

import com.filadelfia.store.filadelfiastore.model.dto.CartDTO;
import com.filadelfia.store.filadelfiastore.model.dto.CartItemDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Cart;

import java.util.List;
import java.util.Optional;

public interface CartService {
    
    // Cart operations
    CartDTO getOrCreateCart(Long userId);
    CartDTO getCartByUserId(Long userId);
    Optional<Cart> findCartByUserId(Long userId);
    void clearCart(Long userId);
    void deleteCart(Long userId);
    
    // Cart item operations
    CartItemDTO addItemToCart(Long userId, Long productId, Integer quantity);
    CartItemDTO updateCartItemQuantity(Long userId, Long productId, Integer quantity);
    void removeItemFromCart(Long userId, Long productId);
    List<CartItemDTO> getCartItems(Long userId);
    
    // Cart calculations
    Integer getTotalItemsInCart(Long userId);
    Double getCartTotal(Long userId);
    
    // Utility methods
    boolean isCartEmpty(Long userId);
    boolean hasProductInCart(Long userId, Long productId);
}
