package com.filadelfia.store.filadelfiastore.service.interfaces;

import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserNewDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDTO createUser(UserNewDTO user);
    List<UserDTO> searchUsers(String searchTerm);
    Optional<UserDTO> getUserById(Long id);
    Optional<UserDTO> getUserByEmail(String email);
    List<UserDTO> getAllUsers();
    Page<UserDTO> getAllUsers(Pageable pageable);
    List<UserDTO> getAllActiveUsers();
    UserDTO updateUser(Long id, UserNewDTO request);
    void deleteUser(Long id);
    boolean existsByEmail(String email);
}