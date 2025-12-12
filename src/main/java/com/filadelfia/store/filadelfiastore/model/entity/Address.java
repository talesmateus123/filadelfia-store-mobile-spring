package com.filadelfia.store.filadelfiastore.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String label; // e.g., "Casa", "Trabalho", "Mãe"
    
    @Column(nullable = false)
    private String street;
    
    @Column(nullable = false)
    private String number;
    
    private String complement;
    
    @Column(nullable = false)
    private String neighborhood;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false, length = 2)
    private String state;
    
    @Column(nullable = false, length = 10)
    private String zipCode;
    
    @Column(name = "is_default")
    private Boolean isDefault = false;
    
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date(System.currentTimeMillis());
    
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date(System.currentTimeMillis());
    
    // Constructor
    public Address(User user, String label, String street, String number, String complement, 
                   String neighborhood, String city, String state, String zipCode) {
        this.user = user;
        this.label = label;
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.createdAt = new Date(System.currentTimeMillis());
        this.updatedAt = new Date(System.currentTimeMillis());
    }
    
    // Business methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append(street).append(", ").append(number);
        if (complement != null && !complement.trim().isEmpty()) {
            address.append(", ").append(complement);
        }
        address.append(", ").append(neighborhood);
        address.append(", ").append(city).append(" - ").append(state);
        address.append(", CEP: ").append(zipCode);
        return address.toString();
    }
    
    public String getShortAddress() {
        return street + ", " + number + " - " + neighborhood + ", " + city + "/" + state;
    }
}