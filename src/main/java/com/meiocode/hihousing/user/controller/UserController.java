
package com.meiocode.hihousing.user.controller;

import com.meiocode.hihousing.common.api.ApiResponse;
import com.meiocode.hihousing.user.dto.CreateUserRequest;
import com.meiocode.hihousing.user.dto.UpdateUserRequest;
import com.meiocode.hihousing.user.dto.UserResponse;
import com.meiocode.hihousing.user.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        // POST /api/users
        @PostMapping
        public ResponseEntity<ApiResponse<UserResponse>> createUser(
                        @Valid @RequestBody CreateUserRequest request) {
                UserResponse user = userService.createUser(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("User created successfully", user));
        }

        // GET /api/users/{id}
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<UserResponse>> getUser(
                        @PathVariable Long id) {
                UserResponse user = userService.getUser(id);

                return ResponseEntity.ok(
                                ApiResponse.success("User retrieved successfully", user));
        }

        // GET /api/users
        @GetMapping
        public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
                List<UserResponse> users = userService.getAllUsers();

                return ResponseEntity.ok(
                                ApiResponse.success("Users retrieved successfully", users));
        }

        // PUT /api/users/{id}
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<UserResponse>> updateUser(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateUserRequest request) {
                UserResponse user = userService.updateUser(id, request);

                return ResponseEntity.ok(
                                ApiResponse.success("User updated successfully", user));
        }

        // DELETE /api/users/{id}
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteUser(
                        @PathVariable Long id) {
                userService.deleteUser(id);

                return ResponseEntity.ok(
                                ApiResponse.success("User deleted successfully", null));
        }
}
