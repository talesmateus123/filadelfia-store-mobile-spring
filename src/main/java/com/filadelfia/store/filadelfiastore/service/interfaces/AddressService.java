package com.filadelfia.store.filadelfiastore.service.interfaces;

import com.filadelfia.store.filadelfiastore.model.dto.AddressDTO;
import java.util.List;

public interface AddressService {
    
    // Address CRUD operations
    AddressDTO createAddress(Long userId, AddressDTO addressDTO);
    AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO);
    void deleteAddress(Long userId, Long addressId);
    AddressDTO getAddressById(Long addressId);
    
    // User address management
    List<AddressDTO> getUserAddresses(Long userId);
    AddressDTO getDefaultAddress(Long userId);
    AddressDTO setDefaultAddress(Long userId, Long addressId);
    
    // Address validation
    boolean validateZipCode(String zipCode);
    AddressDTO formatAddress(AddressDTO addressDTO);
    
    // Statistics
    Long countUserAddresses(Long userId);
}