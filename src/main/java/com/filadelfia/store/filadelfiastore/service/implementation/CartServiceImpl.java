package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.CartDTO;
import com.filadelfia.store.filadelfiastore.model.dto.CartItemDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Cart;
import com.filadelfia.store.filadelfiastore.model.entity.CartItem;
import com.filadelfia.store.filadelfiastore.model.entity.Product;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.mapper.CartMapper;
import com.filadelfia.store.filadelfiastore.repository.CartItemRepository;
import com.filadelfia.store.filadelfiastore.repository.CartRepository;
import com.filadelfia.store.filadelfiastore.repository.ProductRepository;
import com.filadelfia.store.filadelfiastore.repository.UserRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    
    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private CartMapper cartMapper;

    public CartServiceImpl(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartMapper = cartMapper;
    }
    
    @Override
    public CartDTO getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Optional<Cart> existingCart = cartRepository.findByUserId(userId);
        if (existingCart.isPresent()) {
            return cartMapper.toDTO(existingCart.get());
        }
        
        // Create new cart
        Cart newCart = new Cart(user);
        newCart = cartRepository.save(newCart);
        return cartMapper.toDTO(newCart);
    }
    
    @Override
    public CartDTO getCartByUserId(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserIdWithItems(userId);
        if (cart.isPresent()) {
            return cartMapper.toDTO(cart.get());
        }
        return getOrCreateCart(userId);
    }
    
    @Override
    public Optional<Cart> findCartByUserId(Long userId) {
        return cartRepository.findByUserIdWithItems(userId);
    }
    
    @Override
    public void clearCart(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isPresent()) {
            cart.get().clear();
            cartRepository.save(cart.get());
        }
    }
    
    @Override
    public void deleteCart(Long userId) {
        cartRepository.deleteByUserId(userId);
    }
    
    @Override
    public CartItemDTO addItemToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        
        Cart cart = getOrCreateCartEntity(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);
        
        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.calculateSubtotal();
            cartItemRepository.save(item);
            cart.calculateTotal();
            cartRepository.save(cart);
            return cartMapper.toCartItemDTO(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem(cart, product, quantity, product.getPrice());
            newItem.calculateSubtotal();
            cartItemRepository.save(newItem);
            cart.addItem(newItem);
            cartRepository.save(cart);
            return cartMapper.toCartItemDTO(newItem);
        }
    }
    
    @Override
    public CartItemDTO updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeItemFromCart(userId, productId);
            return null;
        }
        
        Cart cart = getOrCreateCartEntity(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        item.setQuantity(quantity);
        item.calculateSubtotal();
        cartItemRepository.save(item);
        cart.calculateTotal();
        cartRepository.save(cart);
        
        return cartMapper.toCartItemDTO(item);
    }
    
    @Override
    public void removeItemFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCartEntity(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        
        CartItem item = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        cart.removeItem(item);
        cartItemRepository.delete(item);
        cartRepository.save(cart);
    }
    
    @Override
    public List<CartItemDTO> getCartItems(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return cartMapper.toCartItemDTOList(items);
    }
    
    @Override
    public Integer getTotalItemsInCart(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        return cart.map(Cart::getTotalItems).orElse(0);
    }
    
    @Override
    public Double getCartTotal(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        return cart.map(c -> c.getTotal().doubleValue()).orElse(0.0);
    }
    
    @Override
    public boolean isCartEmpty(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        return cart.map(Cart::isEmpty).orElse(true);
    }
    
    @Override
    public boolean hasProductInCart(Long userId, Long productId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isPresent()) {
            return cart.get().findItemByProductId(productId) != null;
        }
        return false;
    }
    
    private Cart getOrCreateCartEntity(Long userId) {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isPresent()) {
            return cart.get();
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Cart newCart = new Cart(user);
        return cartRepository.save(newCart);
    }
}
