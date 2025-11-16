package com.filadelfia.store.filadelfiastore.repository;

import com.filadelfia.store.filadelfiastore.model.entity.CartItem;
import com.filadelfia.store.filadelfiastore.model.entity.Cart;
import com.filadelfia.store.filadelfiastore.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    List<CartItem> findByCart(Cart cart);    
    List<CartItem> findByCartId(Long cartId);    
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);    
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.id = :userId")
    List<CartItem> findByUserId(@Param("userId") Long userId);    
    void deleteByCart(Cart cart);    
    void deleteByCartId(Long cartId);    
    boolean existsByCartAndProduct(Cart cart, Product product);
}
