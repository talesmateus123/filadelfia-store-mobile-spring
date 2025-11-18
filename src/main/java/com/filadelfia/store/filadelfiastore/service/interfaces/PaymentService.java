package com.filadelfia.store.filadelfiastore.service.interfaces;

import com.filadelfia.store.filadelfiastore.model.dto.PaymentDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Payment;
import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for Payment operations
 */
public interface PaymentService {
    
    // Basic CRUD operations
    
    /**
     * Create a new payment
     */
    Payment createPayment(PaymentDTO paymentDTO);
    
    /**
     * Create payment for order
     */
    Payment createPaymentForOrder(Order order, PaymentMethod paymentMethod, BigDecimal amount);
    
    /**
     * Find payment by ID
     */
    Optional<Payment> findById(Long id);
    
    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Find payment by gateway transaction ID
     */
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
    
    /**
     * Update payment
     */
    Payment updatePayment(Long id, PaymentDTO paymentDTO);
    
    /**
     * Delete payment
     */
    void deletePayment(Long id);
    
    // Payment processing operations
    
    /**
     * Process credit card payment
     */
    Payment processCreditCardPayment(PaymentDTO paymentDTO);
    
    /**
     * Process PIX payment
     */
    Payment processPixPayment(PaymentDTO paymentDTO);
    
    /**
     * Process Boleto payment
     */
    Payment processBoletoPayment(PaymentDTO paymentDTO);
    
    /**
     * Process bank transfer payment
     */
    Payment processBankTransferPayment(PaymentDTO paymentDTO);
    
    // Payment status management
    
    /**
     * Update payment status
     */
    Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus);
    
    /**
     * Update payment status by transaction ID
     */
    Payment updatePaymentStatusByTransactionId(String transactionId, PaymentStatus newStatus);
    
    /**
     * Confirm payment (manual confirmation)
     */
    Payment confirmPayment(Long paymentId, String notes);
    
    /**
     * Cancel payment
     */
    Payment cancelPayment(Long paymentId, String reason);
    
    /**
     * Refund payment
     */
    Payment refundPayment(Long paymentId, BigDecimal refundAmount, String reason);
    
    // Payment gateway operations
    
    /**
     * Process gateway callback/webhook
     */
    Payment processGatewayCallback(String gatewayTransactionId, Map<String, Object> callbackData);
    
    /**
     * Validate payment with gateway
     */
    Payment validatePaymentWithGateway(String transactionId);
    
    // Query operations
    
    /**
     * Find payments by order
     */
    List<Payment> findPaymentsByOrder(Long orderId);
    
    /**
     * Find payments by user
     */
    List<Payment> findPaymentsByUser(Long userId);
    
    /**
     * Find payments by user with pagination
     */
    Page<Payment> findPaymentsByUser(Long userId, Pageable pageable);
    
    /**
     * Find payments by status
     */
    List<Payment> findPaymentsByStatus(PaymentStatus status);
    
    /**
     * Find payments by payment method
     */
    List<Payment> findPaymentsByPaymentMethod(PaymentMethod paymentMethod);
    
    /**
     * Find payments created between dates
     */
    List<Payment> findPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Search payments
     */
    Page<Payment> searchPayments(String searchTerm, Pageable pageable);
    
    // Business logic operations
    
    /**
     * Get total paid amount for order
     */
    BigDecimal getTotalPaidAmountForOrder(Long orderId);
    
    /**
     * Check if order is fully paid
     */
    boolean isOrderFullyPaid(Long orderId);
    
    /**
     * Get successful payment for order
     */
    Optional<Payment> getSuccessfulPaymentForOrder(Long orderId);
    
    /**
     * Check payment status from gateway
     */
    PaymentStatus checkPaymentStatusFromGateway(String transactionId);
    
    // Maintenance operations
    
    /**
     * Process expired payments
     */
    List<Payment> processExpiredPayments();
    
    /**
     * Process payments requiring confirmation
     */
    List<Payment> processPaymentsRequiringConfirmation();
    
    /**
     * Retry failed payments
     */
    List<Payment> retryFailedPayments();
    
    // Statistics and reporting
    
    /**
     * Get payment statistics for date range
     */
    PaymentStatistics getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get payment method statistics
     */
    List<PaymentMethodStatistics> getPaymentMethodStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get daily payment summary
     */
    DailyPaymentSummary getDailyPaymentSummary(LocalDateTime date);
    
    // Utility methods
    
    /**
     * Generate unique transaction ID
     */
    String generateTransactionId();
    
    /**
     * Calculate processing fee for payment method
     */
    BigDecimal calculateProcessingFee(PaymentMethod paymentMethod, BigDecimal amount);
    
    /**
     * Validate payment data
     */
    boolean validatePaymentData(PaymentDTO paymentDTO);
    
    // Inner classes for statistics
    
    class PaymentStatistics {
        private long totalPayments;
        private BigDecimal totalAmount;
        private long successfulPayments;
        private long failedPayments;
        private double successRate;
        
        // Constructors
        public PaymentStatistics() {}
        
        public PaymentStatistics(long totalPayments, BigDecimal totalAmount, 
                               long successfulPayments, long failedPayments) {
            this.totalPayments = totalPayments;
            this.totalAmount = totalAmount;
            this.successfulPayments = successfulPayments;
            this.failedPayments = failedPayments;
            this.successRate = totalPayments > 0 ? (double) successfulPayments / totalPayments * 100 : 0;
        }
        
        // Getters and Setters
        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public long getSuccessfulPayments() { return successfulPayments; }
        public void setSuccessfulPayments(long successfulPayments) { this.successfulPayments = successfulPayments; }
        
        public long getFailedPayments() { return failedPayments; }
        public void setFailedPayments(long failedPayments) { this.failedPayments = failedPayments; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
    }
    
    class PaymentMethodStatistics {
        private PaymentMethod paymentMethod;
        private long totalPayments;
        private BigDecimal totalAmount;
        
        // Constructors
        public PaymentMethodStatistics() {}
        
        public PaymentMethodStatistics(PaymentMethod paymentMethod, long totalPayments, BigDecimal totalAmount) {
            this.paymentMethod = paymentMethod;
            this.totalPayments = totalPayments;
            this.totalAmount = totalAmount;
        }
        
        // Getters and Setters
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
    
    class DailyPaymentSummary {
        private LocalDateTime date;
        private long totalPayments;
        private BigDecimal totalAmount;
        private Map<PaymentMethod, Long> paymentMethodCounts;
        private Map<PaymentStatus, Long> statusCounts;
        
        // Constructors
        public DailyPaymentSummary() {}
        
        public DailyPaymentSummary(LocalDateTime date, long totalPayments, BigDecimal totalAmount) {
            this.date = date;
            this.totalPayments = totalPayments;
            this.totalAmount = totalAmount;
        }
        
        // Getters and Setters
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        
        public long getTotalPayments() { return totalPayments; }
        public void setTotalPayments(long totalPayments) { this.totalPayments = totalPayments; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public Map<PaymentMethod, Long> getPaymentMethodCounts() { return paymentMethodCounts; }
        public void setPaymentMethodCounts(Map<PaymentMethod, Long> paymentMethodCounts) { this.paymentMethodCounts = paymentMethodCounts; }
        
        public Map<PaymentStatus, Long> getStatusCounts() { return statusCounts; }
        public void setStatusCounts(Map<PaymentStatus, Long> statusCounts) { this.statusCounts = statusCounts; }
    }
}
