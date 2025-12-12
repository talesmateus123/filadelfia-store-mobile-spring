package com.filadelfia.store.filadelfiastore.model.mapper;

import com.filadelfia.store.filadelfiastore.model.dto.AddressDTO;
import com.filadelfia.store.filadelfiastore.model.entity.Address;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AddressMapper {
    
    public AddressDTO toDTO(Address address) {
        if (address == null) {
            return null;
        }
        
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setUserId(address.getUser().getId());
        dto.setUserName(address.getUser().getName());
        dto.setLabel(address.getLabel());
        dto.setStreet(address.getStreet());
        dto.setNumber(address.getNumber());
        dto.setComplement(address.getComplement());
        dto.setNeighborhood(address.getNeighborhood());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setIsDefault(address.getIsDefault());
        dto.setCreatedAt(address.getCreatedAt());
        dto.setUpdatedAt(address.getUpdatedAt());
        
        return dto;
    }
    
    public Address toEntity(AddressDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Address address = new Address();
        address.setId(dto.getId());
        address.setLabel(dto.getLabel());
        address.setStreet(dto.getStreet());
        address.setNumber(dto.getNumber());
        address.setComplement(dto.getComplement());
        address.setNeighborhood(dto.getNeighborhood());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setIsDefault(dto.getIsDefault());
        
        return address;
    }
    
    public List<AddressDTO> toDTOList(List<Address> addresses) {
        return addresses.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}