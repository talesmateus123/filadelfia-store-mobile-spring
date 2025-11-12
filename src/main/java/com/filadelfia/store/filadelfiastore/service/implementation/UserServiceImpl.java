package com.filadelfia.store.filadelfiastore.service.implementation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.filadelfia.store.filadelfiastore.exception.custom.EmailAlreadyExistsException;
import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.mapper.UserMapper;
import com.filadelfia.store.filadelfiastore.repository.UserRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(userDTO);
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    public List<UserDTO> searchUsers(String searchTerm) {
        return userRepository.findByNameContainingIgnoreCaseAndActiveTrue(searchTerm)
            .stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toDTO);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllActiveUsers() {
        return userRepository.findByActiveTrue()
            .stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO request) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If email is being changed, ensure uniqueness
        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.equals(existing.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Copy properties from request to existing entity, ignoring id and password
        BeanUtils.copyProperties(request, existing, "id", "password");
        User updated = userRepository.save(existing);
        return userMapper.toDTO(updated);
    }

    @Override
    public UserDTO updateUserPassword(Long id, String newPassword) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Aqui você pode adicionar validações da senha se necessário
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Considerar criptografar a senha antes de salvar
        existing.setPassword(passwordEncoder.encode(newPassword));
        User updated = userRepository.save(existing);
        return userMapper.toDTO(updated);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
    
}