package com.filadelfia.store.filadelfiastore.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for Customer entity
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    
    private Long id;
    
    // User information (inherited)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phone;
    
    // Customer-specific information
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^\\d{11}$", 
             message = "CPF must be in format 000.000.000-00 or 00000000000")
    private String cpf;
    
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender must be MALE, FEMALE, or OTHER")
    private String gender;
    
    private Boolean marketingConsent = false;
    
    private Boolean newsletterSubscription = false;
    
    @Pattern(regexp = "^(EMAIL|PHONE|SMS)$", message = "Preferred contact method must be EMAIL, PHONE, or SMS")
    private String preferredContactMethod = "EMAIL";
    
    // Additional information
    private Boolean active = true;
    
    // Related entities (for display purposes)
    private List<AddressDTO> addresses;
    private List<OrderDTO> recentOrders;
    
    // Statistics
    private Integer totalOrders;
    private Integer totalAddresses;
    private Boolean hasCompleteProfile;
    
    // Constructor for customer creation (with user data)
    public CustomerDTO(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.active = true;
        this.marketingConsent = false;
        this.newsletterSubscription = false;
        this.preferredContactMethod = "EMAIL";
    }
    
    // Constructor for customer update (without user data)
    public CustomerDTO(LocalDate dateOfBirth, String cpf, String gender, 
                      Boolean marketingConsent, Boolean newsletterSubscription, 
                      String preferredContactMethod) {
        this.dateOfBirth = dateOfBirth;
        this.cpf = cpf;
        this.gender = gender;
        this.marketingConsent = marketingConsent;
        this.newsletterSubscription = newsletterSubscription;
        this.preferredContactMethod = preferredContactMethod;
    }
    
    // Business methods
    public String getFormattedCpf() {
        if (cpf != null && cpf.length() >= 11) {
            String cleanCpf = cpf.replaceAll("[^\\d]", "");
            if (cleanCpf.length() == 11) {
                return cleanCpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
            }
        }
        return cpf;
    }
    
    public Integer getAge() {
        if (dateOfBirth != null) {
            return LocalDate.now().getYear() - dateOfBirth.getYear();
        }
        return null;
    }
    
    public boolean hasBasicInfo() {
        return name != null && !name.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               phone != null && !phone.trim().isEmpty();
    }
    
    public boolean hasPersonalInfo() {
        return dateOfBirth != null && cpf != null && !cpf.trim().isEmpty();
    }
}
