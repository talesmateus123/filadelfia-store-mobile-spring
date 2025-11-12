package com.filadelfia.store.filadelfiastore.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;

public interface UserService {
    UserDTO createUser(UserDTO request);
    Optional<UserDTO> getUserById(Long id);
    List<UserDTO> getAllUsers();
    List<UserDTO> getAllActiveUsers();
    UserDTO updateUser(Long id, UserDTO request);
    UserDTO updateUserPassword(Long id, String password);
    void deleteUser(Long id);
    List<UserDTO> searchUsers(String searchTerm);
}