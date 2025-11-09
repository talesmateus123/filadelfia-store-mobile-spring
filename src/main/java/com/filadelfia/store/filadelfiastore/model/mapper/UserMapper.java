package com.filadelfia.store.filadelfiastore.model.mapper;

import org.springframework.stereotype.Component;

import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;

import lombok.Builder;

@Component
@Builder
public class UserMapper {
    
    public UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getName(), user.getPassword(), user.getPhone(), user.getActive());
    }
    
    public User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setEmail(userDTO.getEmail());
        user.setName(userDTO.getName());
        user.setPassword(userDTO.getPassword());
        user.setPhone(userDTO.getPhone());
        user.setActive(userDTO.getActive());
        return user;
    }
}
