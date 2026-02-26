package com.filadelfia.store.filadelfiastore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a payment transaction
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "BRL";
    
    // Payment gateway information
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;
    
    @Column(name = "gateway_response")
    private String gatewayResponse;
    
    @Column(name = "gateway_reference")
    private String gatewayReference;
    
    // Credit/Debit Card specific fields
    @Column(name = "card_last_four_digits", length = 4)
    private String cardLastFourDigits;
    
    @Column(name = "card_brand", length = 20)
    private String cardBrand;
    
    @Column(name = "card_holder_name")
    private String cardHolderName;
    
    // PIX specific fields
    @Column(name = "pix_key")
    private String pixKey;
    
    @Column(name = "pix_qr_code", columnDefinition = "TEXT")
    private String pixQrCode;
    
    @Column(name = "pix_copy_paste", columnDefinition = "TEXT")
    private String pixCopyPaste;
    
    // Boleto specific fields
    @Column(name = "boleto_number")
    private String boletoNumber;
    
    @Column(name = "boleto_barcode")
    private String boletoBarcode;
    
    @Column(name = "boleto_due_date")
    private LocalDateTime boletoDueDate;
    
    @Column(name = "boleto_url", columnDefinition = "TEXT")
    private String boletoUrl;
    
    // Timestamps
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;
    
    @Column(name = "captured_at")
    private LocalDateTime capturedAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    // Additional information
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    
    @Column(name = "processing_fee", precision = 10, scale = 2)
    private BigDecimal processingFee = BigDecimal.ZERO;
    
    // JPA callback methods
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        
        // Set expiration time based on payment method
        if (this.expiresAt == null) {
            setDefaultExpiration();
        }
    }
    
    // Business methods
    
    /**
     * Set default expiration time based on payment method
     */
    private void setDefaultExpiration() {
        if (paymentMethod != null) {
            switch (paymentMethod) {
                case BOLETO:
                    this.expiresAt = this.createdAt.plusDays(3);
                    break;
                case PIX:
                    this.expiresAt = this.createdAt.plusMinutes(30);
                    break;
                case CREDIT_CARD:
                case DEBIT_CARD:
                    this.expiresAt = this.createdAt.plusMinutes(15);
                    break;
                default:
                    this.expiresAt = this.createdAt.plusHours(24);
            }
        }
    }
    
    /**
     * Check if payment is expired
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt) && 
               status.isInProgress();
    }
    
    /**
     * Update payment status with timestamp
     */
    public void updateStatus(PaymentStatus newStatus) {
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now();
        
        switch (newStatus) {
            case AUTHORIZED:
                this.authorizedAt = now;
                break;
            case CAPTURED:
                this.capturedAt = now;
                break;
            case CONFIRMED:
                this.confirmedAt = now;
                break;
            case CANCELLED:
            case FAILED:
            case REJECTED:
            case EXPIRED:
                this.cancelledAt = now;
                break;
            case PENDING:
            case PROCESSING:
            case REFUNDED:
                // No specific timestamp for these statuses
                break;
        }
        
        this.updatedAt = now;
    }
    
    /**
     * Get formatted amount for display
     */
    public String getFormattedAmount() {
        return String.format("R$ %.2f", amount);
    }
    
    /**
     * Check if payment can be refunded
     */
    public boolean canBeRefunded() {
        return status.isSuccessful() && refundAmount.compareTo(amount) < 0;
    }
    
    /**
     * Get available refund amount
     */
    public BigDecimal getAvailableRefundAmount() {
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
     * Get masked card number for display
     */
    public String getMaskedCardNumber() {
        if (cardLastFourDigits != null && !cardLastFourDigits.isEmpty()) {
            return "**** **** **** " + cardLastFourDigits;
        }
        return null;
    }
    
    /**
     * Constructor for creating new payment
     */
    public Payment(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        generateTransactionId();
    }
    
    /**
     * Generate unique transaction ID
     */
    private void generateTransactionId() {
        // Format: PAY-YYYYMMDD-HHMMSS-XXX (where XXX is random)
        String timestamp = LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 14);
        String random = String.valueOf((int)(Math.random() * 999));
        this.transactionId = String.format("PAY-%s-%03d", timestamp, Integer.parseInt(random));
    }
}
