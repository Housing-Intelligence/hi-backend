package com.meiocode.hihousing.user.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.meiocode.hihousing.user.dto.CreateUserRequest;
import com.meiocode.hihousing.user.dto.UpdateUserRequest;
import com.meiocode.hihousing.user.dto.UserResponse;
import com.meiocode.hihousing.user.entity.User;
import com.meiocode.hihousing.user.repository.UserRepository;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Create user
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                email,
                request.username().trim(),
                passwordHash
        );

        // .save() will persist the user and return the saved entity
        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    // Get user by ID
    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {

        User user = findUserById(id);

        return toResponse(user);
    }

    // Get all users
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Update user profile
    @Transactional
    public UserResponse updateUser(
            Long id,
            UpdateUserRequest request
    ) {

        User user = findUserById(id);

        String email = normalizeEmail(request.email());

        boolean emailUsedByAnotherUser =
                userRepository.findByEmailIgnoreCase(email)
                        .filter(existing -> !existing.getId().equals(id))
                        .isPresent();

        if (emailUsedByAnotherUser) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        user.updateProfile(
                email,
                request.username().trim()
        );

        User updatedUser = userRepository.save(user);

        return toResponse(updatedUser);
    }

    // Delete user
    @Transactional
    public void deleteUser(Long id) {

        User user = findUserById(id);

        userRepository.delete(user);
    }

    private User findUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}