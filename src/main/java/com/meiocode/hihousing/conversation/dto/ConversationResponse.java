package com.meiocode.hihousing.conversation.dto;

import java.time.Instant;

public record ConversationResponse(
        Long id,
        String title,
        Instant createdAt,
        Instant updatedAt
) {}
