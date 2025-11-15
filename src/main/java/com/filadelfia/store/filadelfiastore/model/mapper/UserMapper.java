package com.filadelfia.store.filadelfiastore.model.mapper;

import java.sql.Date;

import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;

import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserNewDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;

import lombok.Builder;

@Component
@Builder
public class UserMapper {
    
    public UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getCreatedAt(), user.getUpdatedAt(), WordUtils.capitalizeFully(user.getName()), user.getEmail().toLowerCase(), user.getPassword(), user.getRole(), user.getPhone(), user.getActive());
    }
    
    public User toEntity(UserDTO userDTO) {
        User user = new User();
        // Preserve dates from DTO if provided, otherwise use current time
        if (userDTO.getCreatedAt() != null) {
            user.setCreatedAt(userDTO.getCreatedAt());
        } else {
            user.setCreatedAt(new Date(System.currentTimeMillis()));
        }
        user.setUpdatedAt(new Date(System.currentTimeMillis()));
        user.setName(WordUtils.capitalizeFully(userDTO.getName()));
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());
        user.setPhone(userDTO.getPhone());
        user.setActive(userDTO.getActive());
        return user;
    }
    
    public User toEntity(UserNewDTO userDTO) {
        User user = new User();
        // When creating from UserNewDTO, always set new dates
        user.setCreatedAt(new Date(System.currentTimeMillis()));
        user.setUpdatedAt(new Date(System.currentTimeMillis()));
        user.setName(WordUtils.capitalizeFully(userDTO.getName()));
        user.setEmail(userDTO.getEmail().toLowerCase());
        user.setPassword(userDTO.getPassword());
        user.setRole(userDTO.getRole());
        user.setPhone(userDTO.getPhone());
        user.setActive(userDTO.getActive());
        return user;
    }
}
