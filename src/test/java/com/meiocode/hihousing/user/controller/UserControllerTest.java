package com.meiocode.hihousing.user.controller;

import com.meiocode.hihousing.user.dto.CreateUserRequest;
import com.meiocode.hihousing.user.dto.UpdateUserRequest;
import com.meiocode.hihousing.user.dto.UserResponse;
import com.meiocode.hihousing.user.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");

        userResponse = new UserResponse(
                1L,
                "test@example.com",
                "testuser",
                now,
                now
        );
    }

    // POST /users
    @Test
    void shouldCreateUser() throws Exception {

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "username": "testuser",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    // GET /users/{id}
    @Test
    void shouldGetUserById() throws Exception {

        when(userService.getUser(1L))
                .thenReturn(userResponse);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    // GET /users
    @Test
    void shouldGetAllUsers() throws Exception {

        when(userService.getAllUsers())
                .thenReturn(List.of(userResponse));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    // PUT /users/{id}
    @Test
    void shouldUpdateUser() throws Exception {

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(userResponse);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "username": "updateduser"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    // DELETE /users/{id}
    @Test
    void shouldDeleteUser() throws Exception {

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // Validation: invalid email
    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "invalid-email",
                                    "username": "testuser",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // Service exception: user not found
    @Test
    void shouldReturn404WhenUserDoesNotExist() throws Exception {

        when(userService.getUser(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // Service exception: duplicate email
    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already exists"
                ));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                    "username": "testuser",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // Invalid JSON
    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "test@example.com",
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
