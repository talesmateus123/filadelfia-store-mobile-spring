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
        // TODO: Get user profile information and recent orders
        
        return "dashboards/user_profile";
    }
}
