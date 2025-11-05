package com.filadelfia.store.filadelfiastore.service.interfaces;

import java.util.List;

import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;

public interface UserService {
    UserDTO createUser(UserDTO request);
    UserDTO getUserById(Long id);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(Long id, UserDTO request);
    void deleteUser(Long id);
}