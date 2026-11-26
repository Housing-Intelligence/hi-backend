package com.meiocode.hihousing.message.dto;

import com.meiocode.hihousing.message.entity.MessageRole;

import java.time.Instant;

public record MessageResponse(
    Long id,
    Long conversationId,
    MessageRole role,
    String content,
    Instant createdAt
) {
}
