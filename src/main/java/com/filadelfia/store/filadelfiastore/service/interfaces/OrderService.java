package com.filadelfia.store.filadelfiastore.service.interfaces;

import com.filadelfia.store.filadelfiastore.model.dto.OrderDTO;
import com.filadelfia.store.filadelfiastore.model.dto.OrderItemDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.enums.OrderStatus;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    // Order creation and management
    OrderDTO createOrderFromCart(Long userId, PaymentMethod paymentMethod, String shippingAddress);
    OrderDTO createOrder(Long userId, List<OrderItemDTO> items, PaymentMethod paymentMethod, String shippingAddress);
    OrderDTO updateOrderStatus(Long orderId, OrderStatus status);
    OrderDTO getOrderById(Long orderId);
    Optional<Order> findOrderById(Long orderId);
    
    // Order retrieval
    List<OrderDTO> getAllOrders();
    Page<OrderDTO> getAllOrders(Pageable pageable);
    List<OrderDTO> getUserOrders(Long userId);
    Page<OrderDTO> getUserOrders(Long userId, Pageable pageable);
    List<OrderDTO> getOrdersByStatus(OrderStatus status);
    Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable);
    List<OrderDTO> getRecentOrders(int days);
    
    // Order statistics
    Long getTotalOrders();
    Long getTotalOrdersByUser(Long userId);
    Long getOrderCountByStatus(OrderStatus status);
    BigDecimal getTotalRevenue();
    BigDecimal getRevenueFromLastDays(int days);
    BigDecimal getTodaysSales();
    BigDecimal getSalesFromDate(java.time.LocalDate date);
    
    // Order processing
    void processPayment(Long orderId);
    void fulfillOrder(Long orderId);
    void cancelOrder(Long orderId);
    void generateNotaFiscal(Long orderId);
    
    // Utility methods
    boolean canCancelOrder(Long orderId);
    boolean isOrderOwnedByUser(Long orderId, Long userId);
}
