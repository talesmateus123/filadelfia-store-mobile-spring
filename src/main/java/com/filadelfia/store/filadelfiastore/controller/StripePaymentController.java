package com.filadelfia.store.filadelfiastore.controller;

import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.entity.Payment;
import com.filadelfia.store.filadelfiastore.repository.OrderRepository;
import com.filadelfia.store.filadelfiastore.service.implementations.StripePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling Stripe payment operations
 */
@Controller
@RequestMapping("/payments/stripe")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true", matchIfMissing = false)
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;
    private final OrderRepository orderRepository;

    /**
     * Create Stripe Checkout Session for card payments
     */
    @PostMapping("/create-checkout-session")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestParam Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            Session session = stripePaymentService.createCheckoutSession(order);

            Map<String, String> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("url", session.getUrl());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating Stripe checkout session: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create payment session: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Create Payment Intent for direct card payments
     */
    @PostMapping("/create-payment-intent")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestParam Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            PaymentIntent intent = stripePaymentService.createPaymentIntent(order);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("publishableKey", stripePaymentService.getPublicKey());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating Stripe payment intent: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create payment intent: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Create PIX Payment Intent
     */
    @PostMapping("/create-pix-payment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createPixPayment(@RequestParam Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            PaymentIntent intent = stripePaymentService.createPixPaymentIntent(order);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", intent.getClientSecret());
            response.put("publishableKey", stripePaymentService.getPublicKey());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            log.error("Error creating Stripe PIX payment: ", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create PIX payment: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Handle successful payment redirect
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String session_id, 
                               @RequestParam Long order_id,
                               Model model) {
        try {
            Payment payment = stripePaymentService.processSuccessfulPayment(session_id, order_id);
            
            model.addAttribute("payment", payment);
            model.addAttribute("order", payment.getOrder());
            model.addAttribute("success", true);
            model.addAttribute("message", "Pagamento processado com sucesso!");

            return "payment-result";
        } catch (Exception e) {
            log.error("Error processing successful payment: ", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "Erro ao processar pagamento: " + e.getMessage());
            return "payment-result";
        }
    }

    /**
     * Handle payment cancellation redirect
     */
    @GetMapping("/cancel")
    public String paymentCancel(@RequestParam Long order_id, Model model) {
        try {
            Payment payment = stripePaymentService.processCancelledPayment(order_id);
            
            model.addAttribute("payment", payment);
            model.addAttribute("order", payment.getOrder());
            model.addAttribute("success", false);
            model.addAttribute("message", "Pagamento cancelado pelo usuário.");

            return "payment-result";
        } catch (Exception e) {
            log.error("Error processing cancelled payment: ", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "Erro ao processar cancelamento: " + e.getMessage());
            return "payment-result";
        }
    }

    /**
     * Stripe webhook endpoint for payment events
     */
    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<String> stripeWebhook(@RequestBody String payload,
                                               @RequestHeader("Stripe-Signature") String sigHeader) {
        // TODO: Implement webhook verification and payment status updates
        log.info("Received Stripe webhook");
        return ResponseEntity.ok("OK");
    }

    /**
     * Get Stripe public key for frontend
     */
    @GetMapping("/public-key")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getPublicKey() {
        Map<String, String> response = new HashMap<>();
        response.put("publicKey", stripePaymentService.getPublicKey());
        return ResponseEntity.ok(response);
    }
}
