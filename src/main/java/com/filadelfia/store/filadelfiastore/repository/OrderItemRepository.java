package com.filadelfia.store.filadelfiastore.repository;

import com.filadelfia.store.filadelfiastore.model.entity.OrderItem;
import com.filadelfia.store.filadelfiastore.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrder(Order order);    
    List<OrderItem> findByOrderId(Long orderId);    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.id = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);    
    void deleteByOrder(Order order);    
    void deleteByOrderId(Long orderId);
}
