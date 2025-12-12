package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.exception.custom.InvalidOperationException;
import com.filadelfia.store.filadelfiastore.model.dto.PaymentDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Payment;
import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentStatus;
import com.filadelfia.store.filadelfiastore.repository.PaymentRepository;
import com.filadelfia.store.filadelfiastore.repository.OrderRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.PaymentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PaymentService
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    
    // Processing fee rates (in percentage)
    private static final Map<PaymentMethod, BigDecimal> PROCESSING_FEE_RATES = Map.of(
        PaymentMethod.CREDIT_CARD, new BigDecimal("3.5"),
        PaymentMethod.DEBIT_CARD, new BigDecimal("2.0"),
        PaymentMethod.PIX, new BigDecimal("0.5"),
        PaymentMethod.BOLETO, new BigDecimal("2.5"),
        PaymentMethod.BANK_TRANSFER, new BigDecimal("1.0"),
        PaymentMethod.CASH, BigDecimal.ZERO
    );
    
    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }
    
    // Basic CRUD operations
    
    @Override
    public Payment createPayment(PaymentDTO paymentDTO) {
        logger.info("Creating payment for order ID: {}", paymentDTO.getOrderId());
        
        // Validate payment data
        if (!validatePaymentData(paymentDTO)) {
            throw new InvalidOperationException("Invalid payment data provided");
        }
        
        // Find the order
        Order order = orderRepository.findById(paymentDTO.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + paymentDTO.getOrderId()));
        
        // Create payment entity
        Payment payment = new Payment(order, paymentDTO.getPaymentMethod(), paymentDTO.getAmount());
        
        // Set additional fields from DTO
        mapDtoToEntity(paymentDTO, payment);
        
        // Calculate processing fee
        BigDecimal processingFee = calculateProcessingFee(paymentDTO.getPaymentMethod(), paymentDTO.getAmount());
        payment.setProcessingFee(processingFee);
        
        // Save payment
        Payment savedPayment = paymentRepository.save(payment);
        
        logger.info("Payment created successfully with ID: {} and transaction ID: {}", 
                   savedPayment.getId(), savedPayment.getTransactionId());
        
        return savedPayment;
    }
    
    @Override
    public Payment createPaymentForOrder(Order order, PaymentMethod paymentMethod, BigDecimal amount) {
        logger.info("Creating payment for order: {} with method: {}", order.getId(), paymentMethod);
        
        Payment payment = new Payment(order, paymentMethod, amount);
        BigDecimal processingFee = calculateProcessingFee(paymentMethod, amount);
        payment.setProcessingFee(processingFee);
        
        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId) {
        return paymentRepository.findByGatewayTransactionId(gatewayTransactionId);
    }
    
    @Override
    public Payment updatePayment(Long id, PaymentDTO paymentDTO) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        
        // Update only allowed fields
        mapDtoToEntity(paymentDTO, payment);
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        
        if (!payment.getStatus().isFinalState()) {
            throw new InvalidOperationException("Cannot delete payment that is not in final state");
        }
        
        paymentRepository.delete(payment);
    }
    
    // Payment processing operations
    
    @Override
    public Payment processCreditCardPayment(PaymentDTO paymentDTO) {
        logger.info("Processing credit card payment for order: {}", paymentDTO.getOrderId());
        
        Payment payment = createPayment(paymentDTO);
        
        try {
            // Simulate credit card processing
            payment.updateStatus(PaymentStatus.PROCESSING);
            paymentRepository.save(payment);
            
            // Simulate gateway interaction
            boolean approved = simulateCreditCardApproval(paymentDTO);
            
            if (approved) {
                payment.updateStatus(PaymentStatus.AUTHORIZED);
                payment.setGatewayTransactionId("CC-" + System.currentTimeMillis());
                payment.setGatewayResponse("Approved");
                
                // Auto-capture for credit cards
                payment.updateStatus(PaymentStatus.CAPTURED);
                payment.updateStatus(PaymentStatus.CONFIRMED);
            } else {
                payment.updateStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Credit card declined");
            }
            
            return paymentRepository.save(payment);
            
        } catch (Exception e) {
            logger.error("Error processing credit card payment: {}", e.getMessage());
            payment.updateStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            return paymentRepository.save(payment);
        }
    }
    
    @Override
    public Payment processPixPayment(PaymentDTO paymentDTO) {
        logger.info("Processing PIX payment for order: {}", paymentDTO.getOrderId());
        
        Payment payment = createPayment(paymentDTO);
        
        try {
            // Generate PIX QR Code and copy-paste code
            String pixQrCode = generatePixQrCode(paymentDTO.getAmount());
            String pixCopyPaste = generatePixCopyPaste(paymentDTO.getAmount());
            
            payment.setPixQrCode(pixQrCode);
            payment.setPixCopyPaste(pixCopyPaste);
            payment.setPixKey(paymentDTO.getPixKey() != null ? paymentDTO.getPixKey() : "store@filadelfia.com");
            
            // PIX payments are pending until customer makes the payment
            payment.updateStatus(PaymentStatus.PENDING);
            payment.setGatewayTransactionId("PIX-" + System.currentTimeMillis());
            
            return paymentRepository.save(payment);
            
        } catch (Exception e) {
            logger.error("Error processing PIX payment: {}", e.getMessage());
            payment.updateStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            return paymentRepository.save(payment);
        }
    }
    
    @Override
    public Payment processBoletoPayment(PaymentDTO paymentDTO) {
        logger.info("Processing Boleto payment for order: {}", paymentDTO.getOrderId());
        
        Payment payment = createPayment(paymentDTO);
        
        try {
            // Generate Boleto information
            String boletoNumber = generateBoletoNumber();
            String boletoBarcode = generateBoletoBarcode(paymentDTO.getAmount());
            String boletoUrl = generateBoletoUrl(boletoNumber);
            LocalDateTime dueDate = LocalDateTime.now().plusDays(3);
            
            payment.setBoletoNumber(boletoNumber);
            payment.setBoletoBarcode(boletoBarcode);
            payment.setBoletoUrl(boletoUrl);
            payment.setBoletoDueDate(dueDate);
            
            // Boleto payments are pending until payment is made
            payment.updateStatus(PaymentStatus.PENDING);
            payment.setGatewayTransactionId("BOL-" + System.currentTimeMillis());
            
            return paymentRepository.save(payment);
            
        } catch (Exception e) {
            logger.error("Error processing Boleto payment: {}", e.getMessage());
            payment.updateStatus(PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
            return paymentRepository.save(payment);
        }
    }
    
    @Override
    public Payment processBankTransferPayment(PaymentDTO paymentDTO) {
        logger.info("Processing bank transfer payment for order: {}", paymentDTO.getOrderId());
        
        Payment payment = createPayment(paymentDTO);
        
        // Bank transfer requires manual confirmation
        payment.updateStatus(PaymentStatus.PENDING);
        payment.setNotes("Aguardando confirmação da transferência bancária");
        
        return paymentRepository.save(payment);
    }
    
    // Payment status management
    
    @Override
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        return updatePaymentStatusInternal(payment, newStatus);
    }
    
    @Override
    public Payment updatePaymentStatusByTransactionId(String transactionId, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with transaction ID: " + transactionId));
        
        return updatePaymentStatusInternal(payment, newStatus);
    }
    
    private Payment updatePaymentStatusInternal(Payment payment, PaymentStatus newStatus) {
        PaymentStatus oldStatus = payment.getStatus();
        
        // Validate status transition
        if (!isValidStatusTransition(oldStatus, newStatus)) {
            throw new InvalidOperationException(
                String.format("Invalid status transition from %s to %s", oldStatus, newStatus));
        }
        
        payment.updateStatus(newStatus);
        logger.info("Payment {} status updated from {} to {}", 
                   payment.getTransactionId(), oldStatus, newStatus);
        
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment confirmPayment(Long paymentId, String notes) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new InvalidOperationException("Only pending payments can be confirmed");
        }
        
        payment.updateStatus(PaymentStatus.CONFIRMED);
        if (notes != null && !notes.trim().isEmpty()) {
            payment.setNotes(notes);
        }
        
        logger.info("Payment {} confirmed manually", payment.getTransactionId());
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment cancelPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        if (payment.getStatus().isFinalState()) {
            throw new InvalidOperationException("Cannot cancel payment in final state");
        }
        
        payment.updateStatus(PaymentStatus.CANCELLED);
        payment.setFailureReason(reason);
        
        logger.info("Payment {} cancelled: {}", payment.getTransactionId(), reason);
        return paymentRepository.save(payment);
    }
    
    @Override
    public Payment refundPayment(Long paymentId, BigDecimal refundAmount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        
        if (!payment.canBeRefunded()) {
            throw new InvalidOperationException("Payment cannot be refunded");
        }
        
        if (refundAmount.compareTo(payment.getAvailableRefundAmount()) > 0) {
            throw new InvalidOperationException("Refund amount exceeds available amount");
        }
        
        BigDecimal currentRefundAmount = payment.getRefundAmount() != null ? 
            payment.getRefundAmount() : BigDecimal.ZERO;
        payment.setRefundAmount(currentRefundAmount.add(refundAmount));
        
        // If fully refunded, update status
        if (payment.getRefundAmount().compareTo(payment.getAmount()) >= 0) {
            payment.updateStatus(PaymentStatus.REFUNDED);
        }
        
        payment.setNotes((payment.getNotes() != null ? payment.getNotes() + "; " : "") + 
                        "Refund: " + reason);
        
        logger.info("Payment {} refunded amount: {}", payment.getTransactionId(), refundAmount);
        return paymentRepository.save(payment);
    }
    
    // Continue with other methods...
    // [Due to length constraints, I'll continue with the most important methods]
    
    // Query operations
    
    @Override
    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Payment> findPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Payment> findPaymentsByUser(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmountForOrder(Long orderId) {
        List<PaymentStatus> successStatuses = Arrays.asList(
            PaymentStatus.CONFIRMED, PaymentStatus.CAPTURED
        );
        return paymentRepository.getTotalPaidAmountForOrder(orderId, successStatuses);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isOrderFullyPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        BigDecimal totalPaid = getTotalPaidAmountForOrder(orderId);
        return totalPaid.compareTo(order.getTotal()) >= 0;
    }
    
    // Utility methods
    
    @Override
    public String generateTransactionId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.valueOf((int)(Math.random() * 999));
        return String.format("PAY-%s-%03d", timestamp, Integer.parseInt(random));
    }
    
    @Override
    public BigDecimal calculateProcessingFee(PaymentMethod paymentMethod, BigDecimal amount) {
        BigDecimal feeRate = PROCESSING_FEE_RATES.getOrDefault(paymentMethod, BigDecimal.ZERO);
        return amount.multiply(feeRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
    
    @Override
    public boolean validatePaymentData(PaymentDTO paymentDTO) {
        if (paymentDTO == null) return false;
        if (paymentDTO.getOrderId() == null) return false;
        if (paymentDTO.getPaymentMethod() == null) return false;
        if (paymentDTO.getAmount() == null || paymentDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) return false;
        
        // Additional validation based on payment method
        switch (paymentDTO.getPaymentMethod()) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                return paymentDTO.getCardLastFourDigits() != null && 
                       paymentDTO.getCardBrand() != null &&
                       paymentDTO.getCardHolderName() != null;
            default:
                return true;
        }
    }
    
    // Private helper methods
    
    private void mapDtoToEntity(PaymentDTO dto, Payment entity) {
        if (dto.getCardLastFourDigits() != null) entity.setCardLastFourDigits(dto.getCardLastFourDigits());
        if (dto.getCardBrand() != null) entity.setCardBrand(dto.getCardBrand());
        if (dto.getCardHolderName() != null) entity.setCardHolderName(dto.getCardHolderName());
        if (dto.getPixKey() != null) entity.setPixKey(dto.getPixKey());
        if (dto.getNotes() != null) entity.setNotes(dto.getNotes());
    }
    
    private boolean simulateCreditCardApproval(PaymentDTO paymentDTO) {
        // Simulate credit card approval logic
        // In real implementation, this would call payment gateway
        return Math.random() > 0.1; // 90% approval rate
    }
    
    private String generatePixQrCode(BigDecimal amount) {
        // Simulate PIX QR code generation
        return "PIX_QR_" + amount.toString().replace(".", "") + "_" + System.currentTimeMillis();
    }
    
    private String generatePixCopyPaste(BigDecimal amount) {
        // Simulate PIX copy-paste code generation
        return "00020126330014BR.GOV.BCB.PIX0111store@email.com52040000530398654" + 
               String.format("%010.2f", amount).replace(".", "") + "5802BR5913Filadelfia Store6009SAO PAULO";
    }
    
    private String generateBoletoNumber() {
        return String.format("%011d", (long)(Math.random() * 99999999999L));
    }
    
    private String generateBoletoBarcode(BigDecimal amount) {
        return "34191234567890123456789012345678901234567890";
    }
    
    private String generateBoletoUrl(String boletoNumber) {
        return "https://boleto.example.com/view/" + boletoNumber;
    }
    
    private boolean isValidStatusTransition(PaymentStatus from, PaymentStatus to) {
        if (from == to) return true;
        if (from.isFinalState()) return false;
        
        PaymentStatus[] validNextStatuses = from.getPossibleNextStatuses();
        return Arrays.asList(validNextStatuses).contains(to);
    }
    
    // Placeholder implementations for interface completeness
    
    @Override
    public Payment processGatewayCallback(String gatewayTransactionId, Map<String, Object> callbackData) {
        // Implementation for gateway callbacks
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public Payment validatePaymentWithGateway(String transactionId) {
        // Implementation for gateway validation
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public List<Payment> findPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    @Override
    public List<Payment> findPaymentsByPaymentMethod(PaymentMethod paymentMethod) {
        return paymentRepository.findByPaymentMethodOrderByCreatedAtDesc(paymentMethod);
    }
    
    @Override
    public List<Payment> findPaymentsBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }
    
    @Override
    public Page<Payment> searchPayments(String searchTerm, Pageable pageable) {
        return paymentRepository.searchPayments(searchTerm, pageable);
    }
    
    @Override
    public Optional<Payment> getSuccessfulPaymentForOrder(Long orderId) {
        List<PaymentStatus> successStatuses = Arrays.asList(
            PaymentStatus.CONFIRMED, PaymentStatus.CAPTURED
        );
        return paymentRepository.findSuccessfulPaymentForOrder(orderId, successStatuses);
    }
    
    @Override
    public PaymentStatus checkPaymentStatusFromGateway(String transactionId) {
        // Implementation for checking status from gateway
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public List<Payment> processExpiredPayments() {
        List<PaymentStatus> pendingStatuses = Arrays.asList(
            PaymentStatus.PENDING, PaymentStatus.PROCESSING, PaymentStatus.AUTHORIZED
        );
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments(LocalDateTime.now(), pendingStatuses);
        
        for (Payment payment : expiredPayments) {
            payment.updateStatus(PaymentStatus.EXPIRED);
            paymentRepository.save(payment);
        }
        
        return expiredPayments;
    }
    
    @Override
    public List<Payment> processPaymentsRequiringConfirmation() {
        List<PaymentMethod> confirmationMethods = Arrays.asList(
            PaymentMethod.BOLETO, PaymentMethod.BANK_TRANSFER
        );
        return paymentRepository.findPaymentsRequiringConfirmation(confirmationMethods, PaymentStatus.PENDING);
    }
    
    @Override
    public List<Payment> retryFailedPayments() {
        // Implementation for retrying failed payments
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public PaymentStatistics getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<PaymentStatus> successStatuses = Arrays.asList(PaymentStatus.CONFIRMED, PaymentStatus.CAPTURED);
        List<PaymentStatus> failedStatuses = Arrays.asList(PaymentStatus.FAILED, PaymentStatus.CANCELLED, PaymentStatus.EXPIRED, PaymentStatus.REJECTED);
        
        Object[] stats = paymentRepository.getPaymentStatistics(startDate, endDate, successStatuses, failedStatuses);
        
        if (stats != null && stats.length >= 4) {
            long totalPayments = ((Number) stats[0]).longValue();
            BigDecimal totalAmount = (BigDecimal) stats[1];
            long successfulPayments = ((Number) stats[2]).longValue();
            long failedPayments = ((Number) stats[3]).longValue();
            
            return new PaymentStatistics(totalPayments, totalAmount, successfulPayments, failedPayments);
        }
        
        return new PaymentStatistics(0, BigDecimal.ZERO, 0, 0);
    }
    
    @Override
    public List<PaymentMethodStatistics> getPaymentMethodStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<PaymentStatus> successStatuses = Arrays.asList(PaymentStatus.CONFIRMED, PaymentStatus.CAPTURED);
        List<Object[]> stats = paymentRepository.getPaymentMethodStatistics(startDate, endDate, successStatuses);
        
        return stats.stream()
            .map(stat -> new PaymentMethodStatistics(
                (PaymentMethod) stat[0],
                ((Number) stat[1]).longValue(),
                (BigDecimal) stat[2]
            ))
            .collect(Collectors.toList());
    }
    
    @Override
    public DailyPaymentSummary getDailyPaymentSummary(LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        List<Payment> payments = findPaymentsBetweenDates(startOfDay, endOfDay);
        
        long totalPayments = payments.size();
        BigDecimal totalAmount = payments.stream()
            .filter(p -> p.getStatus().isSuccessful())
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        DailyPaymentSummary summary = new DailyPaymentSummary(date, totalPayments, totalAmount);
        
        // Count by payment method
        Map<PaymentMethod, Long> methodCounts = payments.stream()
            .collect(Collectors.groupingBy(Payment::getPaymentMethod, Collectors.counting()));
        summary.setPaymentMethodCounts(methodCounts);
        
        // Count by status
        Map<PaymentStatus, Long> statusCounts = payments.stream()
            .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));
        summary.setStatusCounts(statusCounts);
        
        return summary;
    }
}
