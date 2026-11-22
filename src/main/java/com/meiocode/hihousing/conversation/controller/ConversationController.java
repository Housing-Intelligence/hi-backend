
package com.meiocode.hihousing.conversation.controller;

import com.meiocode.hihousing.common.api.ApiResponse;
import com.meiocode.hihousing.conversation.dto.ConversationResponse;
import com.meiocode.hihousing.conversation.dto.CreateConversationRequest;
import com.meiocode.hihousing.conversation.dto.UpdateConversationRequest;
import com.meiocode.hihousing.conversation.service.ConversationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(
            ConversationService conversationService
    ) {
        this.conversationService = conversationService;
    }

    // Create a conversation
    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> create(
            @Valid @RequestBody CreateConversationRequest request
    ) {
        ConversationResponse response =
                conversationService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Conversation created successfully",
                        response
                ));
    }

    // Get all conversations
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> findAll() {

        List<ConversationResponse> conversations =
                conversationService.findAll();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Conversations retrieved successfully",
                        conversations
                )
        );
    }

    // Get a conversation by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> findById(
            @PathVariable Long id
    ) {
        ConversationResponse response =
                conversationService.findById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Conversation retrieved successfully",
                        response
                )
        );
    }

    // Update conversation title
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateConversationRequest request
    ) {
        ConversationResponse response =
                conversationService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Conversation updated successfully",
                        response
                )
        );
    }

    // Delete a conversation
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        conversationService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Conversation deleted successfully",
                        null
                )
        );
    }
}
