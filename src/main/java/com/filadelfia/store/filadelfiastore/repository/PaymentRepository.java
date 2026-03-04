package com.filadelfia.store.filadelfiastore.repository;

import com.filadelfia.store.filadelfiastore.model.entity.Payment;
import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity operations
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * Find payment by gateway transaction ID
     */
    Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);
    
    /**
     * Find all payments for a specific order
     */
    List<Payment> findByOrderOrderByCreatedAtDesc(Order order);
    
    /**
     * Find payments by order ID
     */
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    /**
     * Find payments by status
     */
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);
    
    /**
     * Find payments by payment method
     */
    List<Payment> findByPaymentMethodOrderByCreatedAtDesc(PaymentMethod paymentMethod);
    
    /**
     * Find payments by status and payment method
     */
    List<Payment> findByStatusAndPaymentMethodOrderByCreatedAtDesc(PaymentStatus status, PaymentMethod paymentMethod);
    
    /**
     * Find payments created between dates
     */
    List<Payment> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find expired payments that are still pending
     */
    @Query("SELECT p FROM Payment p WHERE p.expiresAt < :now AND p.status IN :pendingStatuses")
    List<Payment> findExpiredPayments(@Param("now") LocalDateTime now, 
                                     @Param("pendingStatuses") List<PaymentStatus> pendingStatuses);
    
    /**
     * Find payments requiring confirmation (like Boleto)
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentMethod IN :confirmationMethods AND p.status = :status")
    List<Payment> findPaymentsRequiringConfirmation(@Param("confirmationMethods") List<PaymentMethod> confirmationMethods,
                                                   @Param("status") PaymentStatus status);
    
    /**
     * Get total amount of successful payments for an order
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.order.id = :orderId AND p.status IN :successStatuses")
    BigDecimal getTotalPaidAmountForOrder(@Param("orderId") Long orderId, 
                                         @Param("successStatuses") List<PaymentStatus> successStatuses);
    
    /**
     * Find payments by user (through order relationship)
     */
    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId ORDER BY p.createdAt DESC")
    List<Payment> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find payments by user with pagination
     */
    @Query("SELECT p FROM Payment p WHERE p.order.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Payment> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get payment statistics for date range
     */
    @Query("SELECT " +
           "COUNT(p) as totalPayments, " +
           "COALESCE(SUM(CASE WHEN p.status IN :successStatuses THEN p.amount ELSE 0 END), 0) as totalAmount, " +
           "COALESCE(SUM(CASE WHEN p.status IN :successStatuses THEN 1 ELSE 0 END), 0) as successfulPayments, " +
           "COALESCE(SUM(CASE WHEN p.status IN :failedStatuses THEN 1 ELSE 0 END), 0) as failedPayments " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    Object[] getPaymentStatistics(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate,
                                 @Param("successStatuses") List<PaymentStatus> successStatuses,
                                 @Param("failedStatuses") List<PaymentStatus> failedStatuses);
    
    /**
     * Get payment method statistics
     */
    @Query("SELECT p.paymentMethod, COUNT(p), COALESCE(SUM(p.amount), 0) " +
           "FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status IN :successStatuses " +
           "GROUP BY p.paymentMethod ORDER BY COUNT(p) DESC")
    List<Object[]> getPaymentMethodStatistics(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("successStatuses") List<PaymentStatus> successStatuses);
    
    /**
     * Find successful payment for order
     */
    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status IN :successStatuses ORDER BY p.confirmedAt DESC")
    Optional<Payment> findSuccessfulPaymentForOrder(@Param("orderId") Long orderId, 
                                                   @Param("successStatuses") List<PaymentStatus> successStatuses);
    
    /**
     * Count payments by status for today
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status " +
           "AND p.createdAt >= :startOfDay AND p.createdAt < :endOfDay")
    long countTodayPaymentsByStatus(@Param("status") PaymentStatus status, 
                                  @Param("startOfDay") LocalDateTime startOfDay,
                                  @Param("endOfDay") LocalDateTime endOfDay);
    
    /**
     * Find payments that need processing (pending for more than X minutes)
     */
    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.createdAt < :threshold ORDER BY p.createdAt ASC")
    List<Payment> findPaymentsNeedingProcessing(@Param("status") PaymentStatus status, 
                                              @Param("threshold") LocalDateTime threshold);
    
    /**
     * Search payments by transaction ID or gateway reference
     */
    @Query("SELECT p FROM Payment p WHERE " +
           "p.transactionId LIKE %:searchTerm% OR " +
           "p.gatewayTransactionId LIKE %:searchTerm% OR " +
           "p.gatewayReference LIKE %:searchTerm% " +
           "ORDER BY p.createdAt DESC")
    Page<Payment> searchPayments(@Param("searchTerm") String searchTerm, Pageable pageable);
}
