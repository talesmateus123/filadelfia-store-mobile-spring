package com.filadelfia.store.filadelfiastore.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Payment entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    
    private Long id;
    
    private String transactionId;
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
    
    private PaymentStatus status;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Amount format is invalid")
    private BigDecimal amount;
    
    private String currency = "BRL";
    
    // Gateway information
    private String gatewayTransactionId;
    private String gatewayResponse;
    private String gatewayReference;
    
    // Credit/Debit Card fields
    @Pattern(regexp = "^[0-9]{4}$", message = "Card last four digits must be 4 digits")
    private String cardLastFourDigits;
    
    @Size(max = 20, message = "Card brand cannot exceed 20 characters")
    private String cardBrand;
    
    @Size(max = 100, message = "Card holder name cannot exceed 100 characters")
    private String cardHolderName;
    
    // PIX fields
    private String pixKey;
    private String pixQrCode;
    private String pixCopyPaste;
    
    // Boleto fields
    private String boletoNumber;
    private String boletoBarcode;
    private LocalDateTime boletoDueDate;
    private String boletoUrl;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime authorizedAt;
    private LocalDateTime capturedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiresAt;
    
    // Additional information
    private String notes;
    private String failureReason;
    private BigDecimal refundAmount;
    private BigDecimal processingFee;
    
    // For display purposes
    private String formattedAmount;
    private String maskedCardNumber;
    private boolean expired;
    private boolean canBeRefunded;
    private BigDecimal availableRefundAmount;
    
    // Related entity information
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    
    /**
     * Constructor for payment creation request
     */
    public PaymentDTO(Long orderId, PaymentMethod paymentMethod, BigDecimal amount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.currency = "BRL";
    }
    
    /**
     * Constructor for card payment
     */
    public PaymentDTO(Long orderId, PaymentMethod paymentMethod, BigDecimal amount,
                     String cardLastFourDigits, String cardBrand, String cardHolderName) {
        this(orderId, paymentMethod, amount);
        this.cardLastFourDigits = cardLastFourDigits;
        this.cardBrand = cardBrand;
        this.cardHolderName = cardHolderName;
    }
    
    /**
     * Constructor for PIX payment
     */
    public PaymentDTO(Long orderId, BigDecimal amount, String pixKey) {
        this(orderId, PaymentMethod.PIX, amount);
        this.pixKey = pixKey;
    }
    
    /**
     * Get formatted amount for display
     */
    public String getFormattedAmount() {
        if (amount != null) {
            return String.format("R$ %.2f", amount);
        }
        return "R$ 0,00";
    }
    
    /**
     * Get masked card number for display
     */
    public String getMaskedCardNumber() {
        if (cardLastFourDigits != null && !cardLastFourDigits.isEmpty()) {
            return "**** **** **** " + cardLastFourDigits;
        }
        return null;
    }
    
    /**
     * Check if payment is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt) && 
               (status != null && status.isInProgress());
    }
    
    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        if (status == null || amount == null || refundAmount == null) {
            return false;
        }
        return status.isSuccessful() && refundAmount.compareTo(amount) < 0;
    }
    
    /**
     * Get available refund amount
     */
    public BigDecimal getAvailableRefundAmount() {
        if (amount == null || refundAmount == null) {
            return BigDecimal.ZERO;
        }
        return amount.subtract(refundAmount);
    }
    
    /**
     * Check if payment is card-based
     */
    public boolean isCardPayment() {
        return paymentMethod == PaymentMethod.CREDIT_CARD || 
               paymentMethod == PaymentMethod.DEBIT_CARD;
    }
    
    /**
     * Get CSS class for status display
     */
    public String getStatusCssClass() {
        return status != null ? status.getCssClass() : "badge-secondary";
    }
    
    /**
     * Get payment method description
     */
    public String getPaymentMethodDescription() {
        return paymentMethod != null ? paymentMethod.getDescription() : "";
    }
    
    /**
     * Get time until expiration in minutes
     */
    public long getMinutesUntilExpiration() {
        if (expiresAt == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(expiresAt)) {
            return 0;
        }
        return java.time.Duration.between(now, expiresAt).toMinutes();
    }
    
    /**
     * Check if payment requires immediate action from customer
     */
    public boolean requiresCustomerAction() {
        if (status == null) return false;
        
        return status == PaymentStatus.PENDING && 
               (paymentMethod == PaymentMethod.PIX || paymentMethod == PaymentMethod.BOLETO);
    }
}
