package com.filadelfia.store.filadelfiastore.model.mapper;

import com.filadelfia.store.filadelfiastore.model.dto.CartDTO;
import com.filadelfia.store.filadelfiastore.model.dto.CartItemDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Cart;
import com.filadelfia.store.filadelfiastore.model.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {
    
    public CartDTO toDTO(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUser().getId());
        dto.setUserName(cart.getUser().getName());
        dto.setUserEmail(cart.getUser().getEmail());
        dto.setTotal(cart.getTotal());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());
        
        if (cart.getItems() != null) {
            List<CartItemDTO> itemDTOs = cart.getItems().stream()
                    .map(this::toCartItemDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        return dto;
    }
    
    public CartItemDTO toCartItemDTO(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }
        
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setCartId(cartItem.getCart().getId());
        dto.setProductId(cartItem.getProduct().getId());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setProductPrice(cartItem.getProduct().getPrice());
        dto.setQuantity(cartItem.getQuantity());
        dto.setSubtotal(cartItem.getSubtotal());
        
        return dto;
    }
    
    public List<CartDTO> toDTOList(List<Cart> carts) {
        return carts.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<CartItemDTO> toCartItemDTOList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());
    }
}
