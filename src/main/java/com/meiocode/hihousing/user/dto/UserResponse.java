
package com.meiocode.hihousing.user.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String email,
        String username,
        Instant createdAt,
        Instant updatedAt
) {}
