package com.filadelfia.store.filadelfiastore.service.implementation;

import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.AddressDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Address;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.mapper.AddressMapper;
import com.filadelfia.store.filadelfiastore.repository.AddressRepository;
import com.filadelfia.store.filadelfiastore.repository.UserRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {
    
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;
    
    public AddressServiceImpl(AddressRepository addressRepository, 
                             UserRepository userRepository,
                             AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.addressMapper = addressMapper;
    }
    
    @Override
    public AddressDTO createAddress(Long userId, AddressDTO addressDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        Address address = new Address(user, 
                                     addressDTO.getLabel(),
                                     addressDTO.getStreet(),
                                     addressDTO.getNumber(),
                                     addressDTO.getComplement(),
                                     addressDTO.getNeighborhood(),
                                     addressDTO.getCity(),
                                     addressDTO.getState(),
                                     addressDTO.getZipCode());
        
        // If this is marked as default or user has no addresses, set as default
        if (Boolean.TRUE.equals(addressDTO.getIsDefault()) || 
            !addressRepository.existsByUserIdAndIsDefaultTrue(userId)) {
            setAsDefaultAddress(userId, address);
        }
        
        address = addressRepository.save(address);
        return addressMapper.toDTO(address);
    }
    
    @Override
    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        // Verify ownership
        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }
        
        address.setLabel(addressDTO.getLabel());
        address.setStreet(addressDTO.getStreet());
        address.setNumber(addressDTO.getNumber());
        address.setComplement(addressDTO.getComplement());
        address.setNeighborhood(addressDTO.getNeighborhood());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setZipCode(addressDTO.getZipCode());
        address.setUpdatedAt(new Date(System.currentTimeMillis()));
        
        if (Boolean.TRUE.equals(addressDTO.getIsDefault())) {
            setAsDefaultAddress(userId, address);
        }
        
        address = addressRepository.save(address);
        return addressMapper.toDTO(address);
    }
    
    @Override
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }
        
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);
        
        // If deleted address was default, set another one as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
            }
        }
    }
    
    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        return addressMapper.toDTO(address);
    }
    
    @Override
    public List<AddressDTO> getUserAddresses(Long userId) {
        List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        return addressMapper.toDTOList(addresses);
    }
    
    @Override
    public AddressDTO getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(addressMapper::toDTO)
                .orElse(null);
    }
    
    @Override
    public AddressDTO setDefaultAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + addressId));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }
        
        setAsDefaultAddress(userId, address);
        address = addressRepository.save(address);
        return addressMapper.toDTO(address);
    }
    
    @Override
    public boolean validateZipCode(String zipCode) {
        if (zipCode == null) return false;
        
        // Remove any non-digit characters
        String cleanZipCode = zipCode.replaceAll("\\D", "");
        
        // Brazilian ZIP code format: 8 digits
        return cleanZipCode.length() == 8 && Pattern.matches("\\d{8}", cleanZipCode);
    }
    
    @Override
    public AddressDTO formatAddress(AddressDTO addressDTO) {
        if (addressDTO == null) return null;
        
        // Format ZIP code
        if (addressDTO.getZipCode() != null) {
            String cleanZipCode = addressDTO.getZipCode().replaceAll("\\D", "");
            if (cleanZipCode.length() == 8) {
                addressDTO.setZipCode(cleanZipCode);
            }
        }
        
        // Capitalize names
        if (addressDTO.getStreet() != null) {
            addressDTO.setStreet(capitalizeWords(addressDTO.getStreet()));
        }
        if (addressDTO.getNeighborhood() != null) {
            addressDTO.setNeighborhood(capitalizeWords(addressDTO.getNeighborhood()));
        }
        if (addressDTO.getCity() != null) {
            addressDTO.setCity(capitalizeWords(addressDTO.getCity()));
        }
        if (addressDTO.getState() != null) {
            addressDTO.setState(addressDTO.getState().toUpperCase());
        }
        
        return addressDTO;
    }
    
    @Override
    public Long countUserAddresses(Long userId) {
        return addressRepository.countByUserId(userId);
    }
    
    private void setAsDefaultAddress(Long userId, Address address) {
        // Clear current default
        addressRepository.clearDefaultAddressByUserId(userId);
        address.setIsDefault(true);
    }
    
    private String capitalizeWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        return java.util.Arrays.stream(text.toLowerCase().split("\\s+"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
            .collect(java.util.stream.Collectors.joining(" "));
    }
}