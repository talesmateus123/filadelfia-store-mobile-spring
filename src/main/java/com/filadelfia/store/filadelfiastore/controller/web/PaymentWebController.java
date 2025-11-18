package com.filadelfia.store.filadelfiastore.controller.web;

import com.filadelfia.store.filadelfiastore.model.dto.PaymentDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Payment;
import com.filadelfia.store.filadelfiastore.model.entity.Order;

import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentStatus;
import com.filadelfia.store.filadelfiastore.service.interfaces.PaymentService;
import com.filadelfia.store.filadelfiastore.service.interfaces.OrderService;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Web Controller for Payment operations
 */
@Controller
@RequestMapping("/payments")
public class PaymentWebController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentWebController.class);
    
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final UserService userService;
    
    public PaymentWebController(PaymentService paymentService, OrderService orderService, UserService userService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.userService = userService;
    }
    
    /**
     * Show payment form for order
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public String showPaymentForm(@PathVariable Long orderId, 
                                @AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        try {
            Optional<Order> orderOpt = orderService.findOrderById(orderId);
            if (orderOpt.isEmpty()) {
                model.addAttribute("error", "Pedido não encontrado");
                return "error/404";
            }
            
            Order order = orderOpt.get();
            
            // Check if user owns this order
            if (!order.getUser().getEmail().equals(userDetails.getUsername())) {
                model.addAttribute("error", "Acesso negado");
                return "error/403";
            }
            
            // Check if order is already paid
            if (paymentService.isOrderFullyPaid(orderId)) {
                model.addAttribute("message", "Este pedido já foi pago");
                return "redirect:/orders/" + orderId;
            }
            
            PaymentDTO paymentDTO = new PaymentDTO(orderId, null, order.getTotal());
            
            model.addAttribute("order", order);
            model.addAttribute("paymentDTO", paymentDTO);
            model.addAttribute("paymentMethods", PaymentMethod.values());
            
            return "payments/payment-form";
            
        } catch (Exception e) {
            logger.error("Error showing payment form for order {}: {}", orderId, e.getMessage());
            model.addAttribute("error", "Erro interno do servidor");
            return "error/500";
        }
    }
    
    /**
     * Process payment submission
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('USER')")
    public String processPayment(@Valid @ModelAttribute PaymentDTO paymentDTO,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            // Validate order ownership
            Optional<Order> orderOpt = orderService.findOrderById(paymentDTO.getOrderId());
            if (orderOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Pedido não encontrado");
                return "redirect:/orders";
            }
            
            Order order = orderOpt.get();
            if (!order.getUser().getEmail().equals(userDetails.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Acesso negado");
                return "redirect:/orders";
            }
            
            // Process payment based on method
            Payment payment;
            switch (paymentDTO.getPaymentMethod()) {
                case CREDIT_CARD:
                case DEBIT_CARD:
                    payment = paymentService.processCreditCardPayment(paymentDTO);
                    break;
                case PIX:
                    payment = paymentService.processPixPayment(paymentDTO);
                    break;
                case BOLETO:
                    payment = paymentService.processBoletoPayment(paymentDTO);
                    break;
                case BANK_TRANSFER:
                    payment = paymentService.processBankTransferPayment(paymentDTO);
                    break;
                default:
                    payment = paymentService.createPayment(paymentDTO);
            }
            
            redirectAttributes.addFlashAttribute("success", "Pagamento iniciado com sucesso");
            return "redirect:/payments/" + payment.getId();
            
        } catch (Exception e) {
            logger.error("Error processing payment: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao processar pagamento: " + e.getMessage());
            return "redirect:/payments/order/" + paymentDTO.getOrderId();
        }
    }
    
    /**
     * Show payment details
     */
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER')")
    public String showPaymentDetails(@PathVariable Long paymentId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        try {
            Optional<Payment> paymentOpt = paymentService.findById(paymentId);
            if (paymentOpt.isEmpty()) {
                model.addAttribute("error", "Pagamento não encontrado");
                return "error/404";
            }
            
            Payment payment = paymentOpt.get();
            
            // Check if user owns this payment
            if (!payment.getOrder().getUser().getEmail().equals(userDetails.getUsername())) {
                model.addAttribute("error", "Acesso negado");
                return "error/403";
            }
            
            model.addAttribute("payment", payment);
            model.addAttribute("order", payment.getOrder());
            
            // Add payment method specific data
            if (payment.getPaymentMethod() == PaymentMethod.PIX) {
                model.addAttribute("showPixCode", true);
            } else if (payment.getPaymentMethod() == PaymentMethod.BOLETO) {
                model.addAttribute("showBoletoInfo", true);
            }
            
            return "payments/payment-details";
            
        } catch (Exception e) {
            logger.error("Error showing payment details for payment {}: {}", paymentId, e.getMessage());
            model.addAttribute("error", "Erro interno do servidor");
            return "error/500";
        }
    }
    
    /**
     * Show user's payment history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public String showPaymentHistory(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        try {
            // Get user ID from UserDetails (assuming email-based lookup)
            // In a real app, you'd get the user ID more directly
            Optional<UserDTO> userOpt = userService.getUserByEmail(userDetails.getUsername());
            if (userOpt.isEmpty()) {
                model.addAttribute("error", "Usuário não encontrado");
                return "error/404";
            }
            
            Long userId = userOpt.get().getId();
            
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Payment> payments = paymentService.findPaymentsByUser(userId, pageable);
            
            model.addAttribute("payments", payments);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", payments.getTotalPages());
            
            return "payments/payment-history";
            
        } catch (Exception e) {
            logger.error("Error showing payment history: {}", e.getMessage());
            model.addAttribute("error", "Erro interno do servidor");
            return "error/500";
        }
    }
    
    // Admin/Manager endpoints
    
    /**
     * Show payments management dashboard (Admin/Manager only)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String showPaymentsAdmin(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) String search,
                                  @RequestParam(required = false) PaymentStatus status,
                                  @RequestParam(required = false) PaymentMethod method,
                                  Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Payment> payments;
            
            if (search != null && !search.trim().isEmpty()) {
                payments = paymentService.searchPayments(search, pageable);
            } else {
                // Apply filters if needed
                // For now, get all payments
                payments = paymentService.findPaymentsByUser(null, pageable); // This would need to be changed to findAll
            }
            
            // Get statistics
            LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime endOfToday = startOfToday.plusDays(1);
            PaymentService.PaymentStatistics todayStats = paymentService.getPaymentStatistics(startOfToday, endOfToday);
            
            model.addAttribute("payments", payments);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", payments.getTotalPages());
            model.addAttribute("search", search);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("selectedMethod", method);
            model.addAttribute("paymentStatuses", PaymentStatus.values());
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("todayStats", todayStats);
            
            return "admin/payments/payments-list";
            
        } catch (Exception e) {
            logger.error("Error showing payments admin: {}", e.getMessage());
            model.addAttribute("error", "Erro interno do servidor");
            return "error/500";
        }
    }
    
    /**
     * Confirm payment manually (Admin/Manager only)
     */
    @PostMapping("/{paymentId}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String confirmPayment(@PathVariable Long paymentId,
                               @RequestParam(required = false) String notes,
                               RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.confirmPayment(paymentId, notes);
            redirectAttributes.addFlashAttribute("success", 
                "Pagamento " + payment.getTransactionId() + " confirmado com sucesso");
            
        } catch (Exception e) {
            logger.error("Error confirming payment {}: {}", paymentId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao confirmar pagamento: " + e.getMessage());
        }
        
        return "redirect:/payments/admin";
    }
    
    /**
     * Cancel payment (Admin/Manager only)
     */
    @PostMapping("/{paymentId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String cancelPayment(@PathVariable Long paymentId,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId, reason);
            redirectAttributes.addFlashAttribute("success", 
                "Pagamento " + payment.getTransactionId() + " cancelado com sucesso");
                
        } catch (Exception e) {
            logger.error("Error cancelling payment {}: {}", paymentId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar pagamento: " + e.getMessage());
        }
        
        return "redirect:/payments/admin";
    }
    
    /**
     * Process refund (Admin only)
     */
    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public String refundPayment(@PathVariable Long paymentId,
                              @RequestParam BigDecimal refundAmount,
                              @RequestParam String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            Payment payment = paymentService.refundPayment(paymentId, refundAmount, reason);
            redirectAttributes.addFlashAttribute("success", 
                String.format("Reembolso de R$ %.2f processado para pagamento %s", 
                             refundAmount, payment.getTransactionId()));
                             
        } catch (Exception e) {
            logger.error("Error refunding payment {}: {}", paymentId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erro ao processar reembolso: " + e.getMessage());
        }
        
        return "redirect:/payments/admin";
    }
    
    /**
     * Show payment statistics (Admin/Manager only)
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public String showPaymentStatistics(@RequestParam(required = false) String period,
                                      Model model) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate;
            
            // Default to last 30 days
            switch (period != null ? period : "30days") {
                case "7days":
                    startDate = endDate.minusDays(7);
                    break;
                case "30days":
                    startDate = endDate.minusDays(30);
                    break;
                case "90days":
                    startDate = endDate.minusDays(90);
                    break;
                case "1year":
                    startDate = endDate.minusYears(1);
                    break;
                default:
                    startDate = endDate.minusDays(30);
            }
            
            PaymentService.PaymentStatistics stats = paymentService.getPaymentStatistics(startDate, endDate);
            List<PaymentService.PaymentMethodStatistics> methodStats = 
                paymentService.getPaymentMethodStatistics(startDate, endDate);
            
            model.addAttribute("stats", stats);
            model.addAttribute("methodStats", methodStats);
            model.addAttribute("selectedPeriod", period != null ? period : "30days");
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
            return "admin/payments/payment-statistics";
            
        } catch (Exception e) {
            logger.error("Error showing payment statistics: {}", e.getMessage());
            model.addAttribute("error", "Erro interno do servidor");
            return "error/500";
        }
    }
    
    /**
     * Handle payment gateway callbacks/webhooks
     */
    @PostMapping("/webhook/{gateway}")
    public String handlePaymentWebhook(@PathVariable String gateway,
                                     @RequestBody String payload) {
        try {
            logger.info("Received payment webhook from gateway: {}", gateway);
            // Process the webhook based on gateway
            // This would parse the payload and update payment status
            
            return "success"; // Return appropriate response for the gateway
            
        } catch (Exception e) {
            logger.error("Error processing payment webhook from {}: {}", gateway, e.getMessage());
            return "error";
        }
    }
}
