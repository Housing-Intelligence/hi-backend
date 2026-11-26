package com.meiocode.hihousing.message.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateMessageRequest (
    @NotBlank(message = "Message content cannot be blank")
    String content
) {
}