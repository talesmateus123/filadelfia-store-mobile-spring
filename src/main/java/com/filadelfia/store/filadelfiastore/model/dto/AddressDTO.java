package com.filadelfia.store.filadelfiastore.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    
    private Long id;
    private Long userId;
    private String userName;
    private String label;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;
    private String zipCode;
    private Boolean isDefault;
    private Date createdAt;
    private Date updatedAt;
    
    // Helper methods
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
    
    public String getFormattedZipCode() {
        if (zipCode != null && zipCode.length() >= 8) {
            return zipCode.substring(0, 5) + "-" + zipCode.substring(5);
        }
        return zipCode;
    }
}