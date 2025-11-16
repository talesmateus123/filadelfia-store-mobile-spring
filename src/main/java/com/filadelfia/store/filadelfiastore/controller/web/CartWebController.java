package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.CartDTO;

import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.service.interfaces.CartService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
@PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
public class CartWebController {
    
    private CartService cartService;

    public CartWebController(CartService cartService) {
        this.cartService = cartService;
    }
    
    @GetMapping
    public String viewCart(Model model) {
        Long userId = getCurrentUserId();
        CartDTO cart = cartService.getCartByUserId(userId);
        
        model.addAttribute("cart", cart);
        model.addAttribute("totalItems", cart.getTotalItems());
        model.addAttribute("activePage", "cart");
        model.addAttribute("pageTitle", "Carrinho de Compras");
        
        return "cart/shopping_cart";
    }
    
    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, 
                           @RequestParam(defaultValue = "1") Integer quantity,
                           RedirectAttributes redirectAttributes,
                           @RequestHeader(value = "referer", required = false) String referer) {
        try {
            Long userId = getCurrentUserId();
            cartService.addItemToCart(userId, productId, quantity);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Produto adicionado ao carrinho com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao adicionar produto ao carrinho: " + e.getMessage());
        }
        
        // Redirect back to the page where the user came from, or to shop if not available
        if (referer != null && !referer.contains("/cart/add")) {
            return "redirect:" + referer.substring(referer.indexOf("/", 8)); // Remove domain part
        }
        return "redirect:/shop";
    }
    
    @PostMapping("/update")
    public String updateCartItem(@RequestParam Long productId, 
                                @RequestParam Integer quantity,
                                RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId();
            
            if (quantity <= 0) {
                cartService.removeItemFromCart(userId, productId);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Item removido do carrinho!");
            } else {
                cartService.updateCartItemQuantity(userId, productId, quantity);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Quantidade atualizada!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao atualizar item: " + e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId();
            cartService.removeItemFromCart(userId, productId);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Item removido do carrinho!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao remover item: " + e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    @PostMapping("/clear")
    public String clearCart(RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId();
            cartService.clearCart(userId);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Carrinho limpo com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao limpar carrinho: " + e.getMessage());
        }
        
        return "redirect:/cart";
    }
    
    @GetMapping("/checkout")
    public String checkout(Model model) {
        Long userId = getCurrentUserId();
        CartDTO cart = cartService.getCartByUserId(userId);
        
        if (cart.isEmpty()) {
            return "redirect:/cart?error=empty";
        }
        
        model.addAttribute("cart", cart);
        model.addAttribute("activePage", "cart");
        model.addAttribute("pageTitle", "Finalizar Pedido");
        
        return "cart/checkout";
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return ((User) auth.getPrincipal()).getId();
        }
        throw new RuntimeException("User not authenticated");
    }
}
