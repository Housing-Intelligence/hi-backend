
package com.meiocode.hihousing.user.service;

import com.meiocode.hihousing.user.dto.CreateUserRequest;
import com.meiocode.hihousing.user.dto.UpdateUserRequest;
import com.meiocode.hihousing.user.dto.UserResponse;
import com.meiocode.hihousing.user.entity.User;
import com.meiocode.hihousing.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    private final Instant now =
            Instant.parse("2026-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        // Only create the mock here.
        // Configure stubbings inside individual tests.
        existingUser = mock(User.class);
    }

    // Helper: configure fields needed by toResponse()
    private void stubUserResponse(User user, Long id,
                                  String email, String username) {

        when(user.getId()).thenReturn(id);
        when(user.getEmail()).thenReturn(email);
        when(user.getUsername()).thenReturn(username);
        when(user.getCreatedAt()).thenReturn(now);
        when(user.getUpdatedAt()).thenReturn(now);
    }

    // --------------------------------------------------
    // Create User
    // --------------------------------------------------

    @Test
    void shouldCreateUserSuccessfully() {

        CreateUserRequest request = new CreateUserRequest(
                " TEST@EXAMPLE.COM ",
                " testuser ",
                "password123"
        );

        User savedUser = mock(User.class);

        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        stubUserResponse(
                savedUser,
                1L,
                "test@example.com",
                "testuser"
        );

        UserResponse response = userService.createUser(request);

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User capturedUser = captor.getValue();

        assertThat(capturedUser.getEmail())
                .isEqualTo("test@example.com");

        assertThat(capturedUser.getUsername())
                .isEqualTo("testuser");

        assertThat(capturedUser.getPasswordHash())
                .isEqualTo("hashedPassword");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");

        verify(passwordEncoder).encode("password123");
    }

    @Test
    void shouldThrow409WhenEmailAlreadyExists() {

        CreateUserRequest request = new CreateUserRequest(
                "test@example.com",
                "testuser",
                "password123"
        );

        when(userRepository.existsByEmailIgnoreCase("test@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT);
                });

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(any());
    }

    // --------------------------------------------------
    // Get User
    // --------------------------------------------------

    @Test
    void shouldGetUserById() {

        stubUserResponse(
                existingUser,
                1L,
                "test@example.com",
                "testuser"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        UserResponse response = userService.getUser(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.username()).isEqualTo("testuser");
    }

    @Test
    void shouldThrow404WhenUserDoesNotExist() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    // --------------------------------------------------
    // Get All Users
    // --------------------------------------------------

    @Test
    void shouldGetAllUsers() {

        User secondUser = mock(User.class);

        stubUserResponse(
                existingUser,
                1L,
                "test@example.com",
                "testuser"
        );

        stubUserResponse(
                secondUser,
                2L,
                "second@example.com",
                "seconduser"
        );

        when(userRepository.findAll())
                .thenReturn(List.of(existingUser, secondUser));

        List<UserResponse> responses =
                userService.getAllUsers();

        assertThat(responses).hasSize(2);

        assertThat(responses.get(0).email())
                .isEqualTo("test@example.com");

        assertThat(responses.get(1).email())
                .isEqualTo("second@example.com");
    }

    // --------------------------------------------------
    // Update User
    // --------------------------------------------------

    @Test
    void shouldUpdateUserSuccessfully() {

        UpdateUserRequest request = new UpdateUserRequest(
                " TEST@EXAMPLE.COM ",
                " updateduser "
        );

        stubUserResponse(
                existingUser,
                1L,
                "test@example.com",
                "testuser"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.save(existingUser))
                .thenReturn(existingUser);

        UserResponse response =
                userService.updateUser(1L, request);

        verify(existingUser).updateProfile(
                "test@example.com",
                "updateduser"
        );

        verify(userRepository).save(existingUser);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void shouldThrow409WhenUpdatingToAnotherUsersEmail() {

        UpdateUserRequest request = new UpdateUserRequest(
                "another@example.com",
                "updateduser"
        );

        User anotherUser = mock(User.class);

        when(anotherUser.getId()).thenReturn(2L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        when(userRepository.findByEmailIgnoreCase("another@example.com"))
                .thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(
                () -> userService.updateUser(1L, request)
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.CONFLICT);
                });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrow404WhenUpdatingNonExistingUser() {

        UpdateUserRequest request = new UpdateUserRequest(
                "test@example.com",
                "updateduser"
        );

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.updateUser(999L, request)
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    // --------------------------------------------------
    // Delete User
    // --------------------------------------------------

    @Test
    void shouldDeleteUserSuccessfully() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(existingUser));

        userService.deleteUser(1L);

        verify(userRepository).delete(existingUser);
    }

    @Test
    void shouldThrow404WhenDeletingNonExistingUser() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> userService.deleteUser(999L)
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);
                });

        verify(userRepository, never()).delete(any(User.class));
    }
}
