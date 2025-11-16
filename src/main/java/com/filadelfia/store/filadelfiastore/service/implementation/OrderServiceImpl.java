package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.OrderDTO;
import com.filadelfia.store.filadelfiastore.model.dto.OrderItemDTO;
import com.filadelfia.store.filadelfiastore.model.entity.*;
import com.filadelfia.store.filadelfiastore.model.enums.OrderStatus;
import com.filadelfia.store.filadelfiastore.model.enums.PaymentMethod;
import com.filadelfia.store.filadelfiastore.model.mapper.OrderMapper;
import com.filadelfia.store.filadelfiastore.repository.*;
import com.filadelfia.store.filadelfiastore.service.interfaces.CartService;
import com.filadelfia.store.filadelfiastore.service.interfaces.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private CartService cartService;
    private OrderMapper orderMapper;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            CartService cartService,
            OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.orderMapper = orderMapper;
    }
    
    @Override
    public OrderDTO createOrderFromCart(Long userId, PaymentMethod paymentMethod, String shippingAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Get cart with items
        Optional<Cart> cartOpt = cartService.findCartByUserId(userId);
        if (cartOpt.isEmpty() || cartOpt.get().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }
        
        Cart cart = cartOpt.get();
        
        // Create order
        Order order = new Order(user, paymentMethod);
        order.setOrderNumber(generateOrderNumber());
        
        // Parse shipping address (for now, we'll use it as notes)
        order.setNotes(shippingAddress);
        order.setShippingStreet("Default Street");
        order.setShippingNumber("123");
        order.setShippingNeighborhood("Default Neighborhood");
        order.setShippingCity("Default City");
        order.setShippingState("Default State");
        order.setShippingZipCode("00000-000");
        order.setShippingCost(BigDecimal.ZERO);
        
        order = orderRepository.save(order);
        
        // Create order items from cart items
        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(order, cartItem.getProduct(), 
                                               cartItem.getQuantity(), cartItem.getUnitPrice());
            orderItemRepository.save(orderItem);
            order.addItem(orderItem);
        }
        
        order.calculateTotals();
        order = orderRepository.save(order);
        
        // Clear cart after order creation
        cartService.clearCart(userId);
        
        return orderMapper.toDTO(order);
    }
    
    @Override
    public OrderDTO createOrder(Long userId, List<OrderItemDTO> items, PaymentMethod paymentMethod, String shippingAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Order order = new Order(user, paymentMethod);
        order.setOrderNumber(generateOrderNumber());
        
        // Parse shipping address (simplified)
        order.setNotes(shippingAddress);
        order.setShippingStreet("Default Street");
        order.setShippingNumber("123");
        order.setShippingNeighborhood("Default Neighborhood");
        order.setShippingCity("Default City");
        order.setShippingState("Default State");
        order.setShippingZipCode("00000-000");
        order.setShippingCost(BigDecimal.ZERO);
        
        order = orderRepository.save(order);
        
        // Create order items
        for (OrderItemDTO itemDTO : items) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));
            
            OrderItem orderItem = new OrderItem(order, product, itemDTO.getQuantity(), product.getPrice());
            orderItemRepository.save(orderItem);
            order.addItem(orderItem);
        }
        
        order.calculateTotals();
        order = orderRepository.save(order);
        
        return orderMapper.toDTO(order);
    }
    
    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        order.setStatus(status);
        
        if (status == OrderStatus.SHIPPED) {
            order.setShippedAt(new Date(System.currentTimeMillis()));
        } else if (status == OrderStatus.DELIVERED) {
            order.setDeliveredAt(new Date(System.currentTimeMillis()));
        }
        
        order = orderRepository.save(order);
        return orderMapper.toDTO(order);
    }
    
    @Override
    public OrderDTO getOrderById(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findByIdWithItems(orderId);
        if (orderOpt.isEmpty()) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId);
        }
        return orderMapper.toDTO(orderOpt.get());
    }
    
    @Override
    public Optional<Order> findOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }
    
    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAllOrderByCreatedAtDesc();
        return orderMapper.toDTOList(orders);
    }
    
    @Override
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders.getContent());
        return new PageImpl<>(orderDTOs, pageable, orders.getTotalElements());
    }
    
    @Override
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orderMapper.toDTOList(orders);
    }
    
    @Override
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders.getContent());
        return new PageImpl<>(orderDTOs, pageable, orders.getTotalElements());
    }
    
    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orderMapper.toDTOList(orders);
    }
    
    @Override
    public Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders.getContent());
        return new PageImpl<>(orderDTOs, pageable, orders.getTotalElements());
    }
    
    @Override
    public List<OrderDTO> getRecentOrders(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = new Date(cal.getTimeInMillis());
        List<Order> orders = orderRepository.findRecentOrders(startDate);
        return orderMapper.toDTOList(orders);
    }
    
    @Override
    public Long getTotalOrders() {
        return orderRepository.countTotalOrders();
    }
    
    @Override
    public Long getTotalOrdersByUser(Long userId) {
        return orderRepository.countOrdersByUserId(userId);
    }
    
    @Override
    public Long getOrderCountByStatus(OrderStatus status) {
        return orderRepository.countOrdersByStatus(status);
    }
    
    @Override
    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    @Override
    public BigDecimal getRevenueFromLastDays(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        Date startDate = new Date(cal.getTimeInMillis());
        BigDecimal revenue = orderRepository.getRevenueFromDate(startDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    @Override
    public void processPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        order.setPaymentConfirmed(true);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
    
    @Override
    public void fulfillOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedAt(new Date(System.currentTimeMillis()));
        orderRepository.save(order);
    }
    
    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel order that has been shipped or delivered");
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
    
    @Override
    public void generateNotaFiscal(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        
        // TODO: Implement nota fiscal generation logic
        // For now, just set a mock nota fiscal number
        if (order.getStatus() == OrderStatus.DELIVERED) {
            String notaFiscalNumber = "NF-" + order.getOrderNumber();
            // This would require adding a nota fiscal field to Order entity
            // order.setNotaFiscalNumber(notaFiscalNumber);
            orderRepository.save(order);
        }
    }
    
    @Override
    public boolean canCancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }
        
        Order order = orderOpt.get();
        return order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.SHIPPED;
    }
    
    @Override
    public boolean isOrderOwnedByUser(Long orderId, Long userId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }
        
        return orderOpt.get().getUser().getId().equals(userId);
    }
    
    @Override
    public BigDecimal getTodaysSales() {
        LocalDate today = LocalDate.now();
        return getSalesFromDate(today);
    }
    
    @Override
    public BigDecimal getSalesFromDate(LocalDate date) {
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        BigDecimal sales = orderRepository.getSalesFromDate(sqlDate);
        return sales != null ? sales : BigDecimal.ZERO;
    }
    
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
