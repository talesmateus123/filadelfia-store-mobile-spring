package com.filadelfia.store.filadelfiastore.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.filadelfia.store.filadelfiastore.exception.custom.EmailAlreadyExistsException;
import com.filadelfia.store.filadelfiastore.exception.custom.UserNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.mapper.UserMapper;
import com.filadelfia.store.filadelfiastore.repository.UserRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }
    
    @Override
    public UserDTO createUser(UserDTO user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User savedUser = userRepository.save(userMapper.toEntity(user));
        return userMapper.toDTO(savedUser);
    }
    
    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.toDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO request) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found"));

        // If email is being changed, ensure uniqueness
        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.equals(existing.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Copy non-id properties from request to existing entity
        BeanUtils.copyProperties(request, existing, "id");
        User updated = userRepository.save(existing);
        return userMapper.toDTO(updated);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
    
}