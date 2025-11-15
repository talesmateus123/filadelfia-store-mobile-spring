package com.filadelfia.store.filadelfiastore.service.interfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserNewDTO;

public interface UserService {
    UserDTO createUser(UserNewDTO request);
    Optional<UserDTO> getUserById(Long id);
    Optional<UserDTO> getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    Page<UserDTO> getAllUsers(Pageable pageable);
    List<UserDTO> getAllActiveUsers();
    UserDTO updateUser(Long id, UserNewDTO request);
    void deleteUser(Long id);
    List<UserDTO> searchUsers(String searchTerm);
}