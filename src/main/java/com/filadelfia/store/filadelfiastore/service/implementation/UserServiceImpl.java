package com.filadelfia.store.filadelfiastore.service.implementation;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.filadelfia.store.filadelfiastore.exception.custom.EmailAlreadyExistsException;
import com.filadelfia.store.filadelfiastore.exception.custom.ResourceNotFoundException;
import com.filadelfia.store.filadelfiastore.model.dto.UserDTO;
import com.filadelfia.store.filadelfiastore.model.dto.UserNewDTO;
import com.filadelfia.store.filadelfiastore.model.entity.User;
import com.filadelfia.store.filadelfiastore.model.mapper.UserMapper;
import com.filadelfia.store.filadelfiastore.repository.UserRepository;
import com.filadelfia.store.filadelfiastore.service.interfaces.UserService;
import com.filadelfia.store.filadelfiastore.util.PageableValidator;

@Service
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final PageableValidator pageableValidator;

    private final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
        "id", "name", "description", "createdAt", "updatedAt"
    );
    
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, PageableValidator pageableValidator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.pageableValidator = pageableValidator;
    }
    
    @Override
    @Transactional
    public UserDTO createUser(UserNewDTO userNewDTO) {
        if (userRepository.existsByEmail(userNewDTO.getEmail())) {
            throw new EmailAlreadyExistsException("E-mail já existe");
        }

        User user = userMapper.toEntity(userNewDTO);
        user.setPassword(passwordEncoder.encode(userNewDTO.getPassword()));
        
        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String searchTerm) {
        // Validate and sanitize search term
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveUsers();
        }
        
        // Limit search term length to prevent abuse
        String sanitizedTerm = searchTerm.trim();
        if (sanitizedTerm.length() > 100) {
            sanitizedTerm = sanitizedTerm.substring(0, 100);
        }
        
        return userRepository.findByNameContainingIgnoreCaseAndActiveTrue(sanitizedTerm)
            .stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
            .map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.getUserByEmail(email)
            .map(userMapper::toDTO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
            .stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Pageable safePageable = pageableValidator.validateAndSanitize(
            pageable, 
            ALLOWED_SORT_PROPERTIES,
            "name"
        );
        return userRepository.findAll(safePageable)
            .map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllActiveUsers() {
        return userRepository.findByActiveTrue()
            .stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserNewDTO request) {
        User existing = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // If email is being changed, ensure uniqueness
        String newEmail = request.getEmail();
        if (newEmail != null && !newEmail.equals(existing.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("E-mail já existe");
        }

        existing.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));

        // Only encode password if it's provided and different from existing
        String newPassword = request.getPassword();
        if (newPassword != null && !newPassword.isEmpty()) {
            // Check if password is already encoded (BCrypt hashes start with $2a$, $2b$, or $2y$)
            // If not encoded, encode it. If already encoded, don't double-encode.
            if (!newPassword.startsWith("$2a$") && !newPassword.startsWith("$2b$") && !newPassword.startsWith("$2y$")) {
                existing.setPassword(passwordEncoder.encode(newPassword));
            } else {
                // Password appears to be already encoded, use as-is (though this shouldn't happen in normal flow)
                existing.setPassword(newPassword);
            }
        }
        // If password is null or empty, keep the existing password

        // Copy properties from request to existing entity, ignoring id, createdAt, and password
        BeanUtils.copyProperties(request, existing, "id", "createdAt", "password");
        User updated = userRepository.save(existing);
        return userMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        
        // Soft delete: set active to false instead of hard delete
        user.setActive(false);
        user.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        userRepository.save(user);
    }
    
}