package com.filadelfia.store.filadelfiastore.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.filadelfia.store.filadelfiastore.model.enums.OrderStatus;
import com.filadelfia.store.filadelfiastore.service.interfaces.OrderService;
import com.filadelfia.store.filadelfiastore.service.interfaces.ProductService;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

@Controller
public class DashboardController {

    private UserService userService;
    private OrderService orderService;
    private ProductService productService;

    public DashboardController(UserService userService, OrderService orderService, ProductService productService) {
        this.userService = userService;
        this.orderService = orderService;
        this.productService = productService;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("pageTitle", "Painel Administrativo");
        model.addAttribute("activePage", "admin");
        
        // Add admin statistics
        model.addAttribute("totalOrders", orderService.getTotalOrders());
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("recentOrders", orderService.getRecentOrders(7));
        model.addAttribute("pendingOrders", orderService.getOrderCountByStatus(OrderStatus.PENDING));
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        model.addAttribute("totalUsers", userService.getAllUsers().size());

        return "dashboards/admin_dashboard";
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('MANAGER')")
    public String managerDashboard(Model model) {
        model.addAttribute("pageTitle", "Painel do Gerente");
        model.addAttribute("activePage", "manager");
        
        // Add manager statistics
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        model.addAttribute("pendingOrders", orderService.getOrderCountByStatus(OrderStatus.PENDING));
        model.addAttribute("lowStockProducts", productService.getLowStockProductsCount());
        model.addAttribute("todaySales", String.format("R$ %.2f", orderService.getTodaysSales()));
        
        // Add lists for dashboard sections
        model.addAttribute("pendingOrdersList", orderService.getOrdersByStatus(OrderStatus.PENDING));
        model.addAttribute("lowStockProductsList", productService.getLowStockProducts());
        
        return "dashboards/manager_dashboard";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String userProfile(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "Meu Perfil");
        model.addAttribute("activePage", "profile");
        
        // Get current user information
        String email = authentication.getName();
        
        try {
            // Find user by email and get profile data
            var userOptional = userService.getUserByEmail(email);
            
            if (userOptional.isPresent()) {
                var user = userOptional.get();
                model.addAttribute("user", user);
                
                // Get user's order statistics
                model.addAttribute("totalUserOrders", orderService.getTotalOrdersByUser(user.getId()));
                var recentUserOrders = orderService.getUserOrders(user.getId());
                model.addAttribute("recentUserOrders", recentUserOrders.stream().limit(5).toList());
                
                // Calculate total spent
                var totalSpent = recentUserOrders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                    .map(order -> order.getTotal())
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                model.addAttribute("totalSpent", String.format("R$ %.2f", totalSpent));
            } else {
                model.addAttribute("error", "Usuário não encontrado");
            }
        } catch (Exception e) {
            // Handle user not found or other errors
            model.addAttribute("error", "Erro ao carregar dados do perfil: " + e.getMessage());
        }
        
        return "dashboards/user_profile";
    }
}
