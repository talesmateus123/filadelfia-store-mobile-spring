package com.filadelfia.store.filadelfiastore.repository;

import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Find orders by user
    List<Order> findByUserOrderByCreatedAtDesc(User user);    
    // Find orders by user ID
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);    
    // Find orders by status
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);    
    // Find orders by user and status
    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);    
    // Find order by ID and user (for security)
    Optional<Order> findByIdAndUser(Long id, User user);    
    // Find recent orders (for admin dashboard)
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :fromDate ORDER BY o.createdAt DESC")
    List<Order> findRecentOrders(@Param("fromDate") java.sql.Date fromDate);    
    // Count orders by status
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);    
    // Count total orders for user
    Long countByUser(User user);    
    // Find orders by date range
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") java.sql.Date startDate, @Param("endDate") java.sql.Date endDate);    
    // Additional methods needed by OrderServiceImpl
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);    
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    List<Order> findAllOrderByCreatedAtDesc();    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);    
    List<Order> findByStatus(OrderStatus status);    
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);    
    @Query("SELECT COUNT(o) FROM Order o")
    Long countTotalOrders();    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countOrdersByUserId(@Param("userId") Long userId);    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countOrdersByStatus(@Param("status") OrderStatus status);    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();    
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt >= :fromDate")
    BigDecimal getRevenueFromDate(@Param("fromDate") java.sql.Date fromDate);
}
