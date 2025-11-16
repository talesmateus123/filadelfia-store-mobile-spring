package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.CartDTO;
import com.filadelfia.store.filadelfiastore.model.dto.OrderDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.enums.OrderStatus;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.service.interfaces.CartService;
import com.filadelfia.store.filadelfiastore.service.interfaces.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
@RequestMapping("/orders")
public class OrderWebController {
    
    private OrderService orderService;
    private CartService cartService;

    public OrderWebController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }
    
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String myOrders(Model model, 
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        Page<OrderDTO> orders = orderService.getUserOrders(userId, PageRequest.of(page, size));
        
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        
        return "orders/my_orders";
    }
    
    @GetMapping("/manage")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String manageOrders(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) OrderStatus status) {
        Page<OrderDTO> orders;
        
        if (status != null) {
            orders = orderService.getOrdersByStatus(status, PageRequest.of(page, size));
        } else {
            orders = orderService.getAllOrders(PageRequest.of(page, size));
        }
        
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("orderStatuses", OrderStatus.values());
        
        return "orders/manage_orders";
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String viewOrder(@PathVariable Long id, Model model) {
        OrderDTO order = orderService.getOrderById(id);
        
        // Check if user can view this order
        if (!canViewOrder(order)) {
            return "redirect:/orders/my?error=access_denied";
        }
        
        model.addAttribute("order", order);
        model.addAttribute("canCancel", orderService.canCancelOrder(id));
        
        return "orders/order_details";
    }
    
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String createOrder(@RequestParam PaymentMethod paymentMethod,
                             @RequestParam(required = false) Long addressId,
                             @RequestParam(required = false) String shippingStreet,
                             @RequestParam(required = false) String shippingNumber,
                             @RequestParam(required = false) String shippingComplement,
                             @RequestParam(required = false) String shippingNeighborhood,
                             @RequestParam(required = false) String shippingCity,
                             @RequestParam(required = false) String shippingState,
                             @RequestParam(required = false) String shippingZipCode,
                             RedirectAttributes redirectAttributes) {
        try {
            Long userId = getCurrentUserId();
            
            // Check if cart is not empty
            CartDTO cart = cartService.getCartByUserId(userId);
            if (cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Carrinho está vazio!");
                return "redirect:/cart";
            }
            
            OrderDTO order = orderService.createOrderFromCartWithAddress(userId, paymentMethod, 
                addressId, shippingStreet, shippingNumber, shippingComplement, 
                shippingNeighborhood, shippingCity, shippingState, shippingZipCode);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Pedido criado com sucesso! Número: " + order.getOrderNumber());
            return "redirect:/orders/" + order.getId() + "/payment";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao criar pedido: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
    
    @GetMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String showPaymentPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            
            // Check if user owns this order (except for admins/managers)
            if (!isCurrentUserAdminOrManager() && !order.getUserId().equals(getCurrentUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Você não tem permissão para acessar este pedido!");
                return "redirect:/orders/my";
            }
            
            model.addAttribute("order", order);
            model.addAttribute("pageTitle", "Pagamento - Pedido " + order.getOrderNumber());
            model.addAttribute("activePage", "orders");
            
            return "orders/payment";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao carregar página de pagamento: " + e.getMessage());
            return "redirect:/orders/my";
        }
    }
    
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            OrderDTO order = orderService.getOrderById(id);
            
            // Check if user can cancel this order
            if (!canCancelOrder(order)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Você não tem permissão para cancelar este pedido!");
                return "redirect:/orders/" + id;
            }
            
            orderService.cancelOrder(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Pedido cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao cancelar pedido: " + e.getMessage());
        }
        
        return "redirect:/orders/" + id;
    }
    
    @PostMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String updateOrderStatus(@PathVariable Long id, 
                                   @RequestParam OrderStatus status,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Status do pedido atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao atualizar status: " + e.getMessage());
        }
        
        return "redirect:/orders/" + id;
    }
    
    @PostMapping("/{id}/payment/confirm")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.processPayment(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Pagamento confirmado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao confirmar pagamento: " + e.getMessage());
        }
        
        return "redirect:/orders/" + id;
    }
    
    @PostMapping("/{id}/fulfill")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String fulfillOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.fulfillOrder(id);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Pedido enviado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Erro ao enviar pedido: " + e.getMessage());
        }
        
        return "redirect:/orders/" + id;
    }
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            return ((User) auth.getPrincipal()).getId();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    private boolean canViewOrder(OrderDTO order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            
            // Admins and Managers can view all orders
            if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("MANAGER")) {
                return true;
            }
            
            // Users can only view their own orders
            return order.getUserId().equals(user.getId());
        }
        return false;
    }
    
    private boolean canCancelOrder(OrderDTO order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            
            // Check if order can be cancelled
            if (!orderService.canCancelOrder(order.getId())) {
                return false;
            }
            
            // Admins and Managers can cancel any order
            if (user.getRole().name().equals("ADMIN") || user.getRole().name().equals("MANAGER")) {
                return true;
            }
            
            // Users can only cancel their own orders
            return order.getUserId().equals(user.getId());
        }
        return false;
    }
    
    private boolean isCurrentUserAdminOrManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            return user.getRole().name().equals("ADMIN") || user.getRole().name().equals("MANAGER");
        }
        return false;
    }
}
