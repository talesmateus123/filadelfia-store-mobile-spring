package com.filadelfia.store.filadelfiastore.service.implementations;

import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.entity.Payment;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentStatus;
import com.filadelfia.store.filadelfiastore.repository.PaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for handling Stripe payment operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "stripe.enabled", havingValue = "true", matchIfMissing = true)
public class StripePaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${stripe.public.key:}")
    private String publicKey;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Value("${ssl.enabled:false}")
    private boolean sslEnabled;

    /**
     * Create a Stripe Checkout Session for card payments
     */
    public Session createCheckoutSession(Order order) throws StripeException {
        validateStripeConfiguration();
        log.info("Creating Stripe checkout session for order: {}", order.getId());

        String actualBaseUrl = getActualBaseUrl();
        
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(actualBaseUrl + "/payments/stripe/success?session_id={CHECKOUT_SESSION_ID}&order_id=" + order.getId())
                .setCancelUrl(actualBaseUrl + "/payments/stripe/cancel?order_id=" + order.getId())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("brl")
                                                .setUnitAmount((long) (order.getTotal().doubleValue() * 100)) // Convert to cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Pedido #" + order.getId() + " - Filadelfia Store")
                                                                .setDescription("Pagamento do pedido #" + order.getId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("order_id", order.getId().toString())
                .build();

        Session session = Session.create(params);
        log.info("Stripe checkout session created: {}", session.getId());

        // Create payment record
        createPaymentRecord(order, session.getId(), PaymentMethod.STRIPE_CARD);

        return session;
    }

    /**
     * Create a Payment Intent for direct card payments
     */
    public PaymentIntent createPaymentIntent(Order order) throws StripeException {
        validateStripeConfiguration();
        log.info("Creating Stripe payment intent for order: {}", order.getId());

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (order.getTotal().doubleValue() * 100)) // Convert to cents
                .setCurrency("brl")
                .setDescription("Pagamento do pedido #" + order.getId() + " - Filadelfia Store")
                .putMetadata("order_id", order.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("Stripe payment intent created: {}", intent.getId());

        // Create payment record
        createPaymentRecord(order, intent.getId(), PaymentMethod.STRIPE_CARD);

        return intent;
    }

    /**
     * Create a PIX Payment Intent
     */
    public PaymentIntent createPixPaymentIntent(Order order) throws StripeException {
        validateStripeConfiguration();
        log.info("Creating Stripe PIX payment intent for order: {}", order.getId());

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (order.getTotal().doubleValue() * 100))
                .setCurrency("brl")
                .addPaymentMethodType("pix")
                .setDescription("Pagamento PIX do pedido #" + order.getId() + " - Filadelfia Store")
                .putMetadata("order_id", order.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        log.info("Stripe PIX payment intent created: {}", intent.getId());

        // Create payment record
        createPaymentRecord(order, intent.getId(), PaymentMethod.STRIPE_PIX);

        return intent;
    }

    /**
     * Verify and process a successful payment
     */
    public Payment processSuccessfulPayment(String sessionId, Long orderId) throws StripeException {
        log.info("Processing successful payment for session: {} and order: {}", sessionId, orderId);

        Session session = Session.retrieve(sessionId);
        
        if ("paid".equals(session.getPaymentStatus())) {
            Payment payment = paymentRepository.findByGatewayTransactionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));

            payment.setStatus(PaymentStatus.CONFIRMED);
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setGatewayResponse("Payment completed successfully via Stripe");

            return paymentRepository.save(payment);
        } else {
            throw new RuntimeException("Payment not completed. Status: " + session.getPaymentStatus());
        }
    }

    /**
     * Handle payment cancellation
     */
    public Payment processCancelledPayment(Long orderId) {
        log.info("Processing cancelled payment for order: {}", orderId);

        // Find the latest payment for this order
        Payment payment = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setGatewayResponse("Payment cancelled by user");

        return paymentRepository.save(payment);
    }

    /**
     * Create a payment record in the database
     */
    private void createPaymentRecord(Order order, String gatewayTransactionId, PaymentMethod paymentMethod) {
        Payment payment = new Payment();
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotal());
        payment.setCurrency("BRL");
        payment.setGatewayTransactionId(gatewayTransactionId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setGatewayResponse("Stripe payment initiated");

        paymentRepository.save(payment);
        log.info("Payment record created for order: {} with transaction ID: {}", order.getId(), payment.getTransactionId());
    }

    /**
     * Get Stripe public key for frontend
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Get the actual base URL considering SSL configuration
     */
    private String getActualBaseUrl() {
        // If SSL is explicitly disabled, ensure HTTP
        if (!sslEnabled && baseUrl.startsWith("https://")) {
            return baseUrl.replace("https://", "http://");
        }
        // If SSL is enabled, ensure HTTPS
        if (sslEnabled && baseUrl.startsWith("http://")) {
            return baseUrl.replace("http://", "https://");
        }
        return baseUrl;
    }

    /**
     * Validate that Stripe is properly configured
     */
    private void validateStripeConfiguration() {
        if (publicKey == null || publicKey.trim().isEmpty() || publicKey.equals("pk_test_your_publishable_key_here")) {
            throw new IllegalStateException("Stripe public key is not configured. Please set stripe.public.key in your environment variables.");
        }
    }
}
