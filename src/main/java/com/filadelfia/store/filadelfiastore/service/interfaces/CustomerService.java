package com.filadelfia.store.filadelfiastore.service.interfaces;

import com.filadelfia.store.filadelfiastore.model.dto.CustomerDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Customer;
import com.filadelfia.store.filadelfiastore.model.entity.Order;
import com.filadelfia.store.filadelfiastore.model.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Customer management operations
 */
public interface CustomerService {
    
    /**
     * Find customer by ID
     */
    Optional<Customer> findById(Long id);
    
    /**
     * Find customer by email
     */
    Optional<Customer> findByEmail(String email);
    
    /**
     * Find customer by user ID
     */
    Optional<Customer> findByUserId(Long userId);
    
    /**
     * Create a new customer
     */
    Customer createCustomer(CustomerDTO customerDTO);
    
    /**
     * Update customer information
     */
    Customer updateCustomer(Long id, CustomerDTO customerDTO);
    
    /**
     * Get customer profile with complete information
     */
    Customer getCustomerProfile(Long id);
    
    /**
     * Get customer order history
     */
    List<Order> getCustomerOrderHistory(Long customerId);
    
    /**
     * Get customer order history with pagination
     */
    Page<Order> getCustomerOrderHistory(Long customerId, Pageable pageable);
    
    /**
     * Get customer addresses
     */
    List<Address> getCustomerAddresses(Long customerId);
    
    /**
     * Get customer's default address
     */
    Optional<Address> getDefaultAddress(Long customerId);
    
    /**
     * Add address to customer
     */
    Address addAddressToCustomer(Long customerId, Address address);
    
    /**
     * Update customer marketing preferences
     */
    Customer updateMarketingPreferences(Long customerId, Boolean marketingConsent, Boolean newsletterSubscription);
    
    /**
     * Check if customer profile is complete
     */
    boolean isProfileComplete(Long customerId);
    
    /**
     * Get customer statistics
     */
    CustomerStatistics getCustomerStatistics(Long customerId);
    
    /**
     * Delete customer (soft delete)
     */
    void deleteCustomer(Long id);
    
    /**
     * Find all customers with pagination
     */
    Page<Customer> findAllCustomers(Pageable pageable);
    
    /**
     * Search customers by name or email
     */
    Page<Customer> searchCustomers(String searchTerm, Pageable pageable);
    
    /**
     * Customer statistics inner class
     */
    class CustomerStatistics {
        private int totalOrders;
        private int totalAddresses;
        private boolean hasCompleteProfile;
        private boolean marketingConsent;
        private boolean newsletterSubscription;
        
        // Constructors
        public CustomerStatistics() {}
        
        public CustomerStatistics(int totalOrders, int totalAddresses, boolean hasCompleteProfile, 
                                boolean marketingConsent, boolean newsletterSubscription) {
            this.totalOrders = totalOrders;
            this.totalAddresses = totalAddresses;
            this.hasCompleteProfile = hasCompleteProfile;
            this.marketingConsent = marketingConsent;
            this.newsletterSubscription = newsletterSubscription;
        }
        
        // Getters and Setters
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        
        public int getTotalAddresses() { return totalAddresses; }
        public void setTotalAddresses(int totalAddresses) { this.totalAddresses = totalAddresses; }
        
        public boolean isHasCompleteProfile() { return hasCompleteProfile; }
        public void setHasCompleteProfile(boolean hasCompleteProfile) { this.hasCompleteProfile = hasCompleteProfile; }
        
        public boolean isMarketingConsent() { return marketingConsent; }
        public void setMarketingConsent(boolean marketingConsent) { this.marketingConsent = marketingConsent; }
        
        public boolean isNewsletterSubscription() { return newsletterSubscription; }
        public void setNewsletterSubscription(boolean newsletterSubscription) { this.newsletterSubscription = newsletterSubscription; }
    }
}
