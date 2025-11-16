package com.filadelfia.store.filadelfiastore.controller.web;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.filadelfia.store.filadelfiastore.model.dto.ProductDTO;
import com.filadelfia.store.filadelfiastore.service.interfaces.ProductService;

@Controller
@RequestMapping("/")
public class HomeWebController {

    private final ProductService productService;

    public HomeWebController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping()
    public String home(Model model, Authentication authentication) {
        // If user is authenticated, redirect to appropriate dashboard
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                return "redirect:/admin";
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
                return "redirect:/manager";
            } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                return "redirect:/profile";
            }
        }
        
        model.addAttribute("pageTitle", "Bem-vindo à Filadelfia Store");
                
        // Get featured products
        List<ProductDTO> featuredProducts = productService.getFeaturedProducts();
        model.addAttribute("featuredProducts", featuredProducts);
        
        model.addAttribute("activePage", "home");
        return "index";
    }

}