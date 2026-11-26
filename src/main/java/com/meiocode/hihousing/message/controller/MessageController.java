
package com.meiocode.hihousing.message.controller;

import com.meiocode.hihousing.common.api.ApiResponse;
import com.meiocode.hihousing.message.dto.CreateMessageRequest;
import com.meiocode.hihousing.message.dto.MessageResponse;
import com.meiocode.hihousing.message.service.MessageService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations/{conversationId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // Create a user message
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> create(
            @PathVariable Long conversationId,
            @Valid @RequestBody CreateMessageRequest request
    ) {

        MessageResponse response =
                messageService.create(conversationId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Message created successfully",
                        response
                ));
    }

    // Get conversation message history
    @GetMapping
    public ResponseEntity<ApiResponse<List<MessageResponse>>> findAll(
            @PathVariable Long conversationId
    ) {

        List<MessageResponse> messages =
                messageService.findByConversationId(conversationId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Messages retrieved successfully",
                        messages
                )
        );
    }
}
