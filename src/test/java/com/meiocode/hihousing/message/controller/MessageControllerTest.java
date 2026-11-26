
package com.meiocode.hihousing.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meiocode.hihousing.common.exception.GlobalExceptionHandler;
import com.meiocode.hihousing.message.dto.CreateMessageRequest;
import com.meiocode.hihousing.message.dto.MessageResponse;
import com.meiocode.hihousing.message.entity.MessageRole;
import com.meiocode.hihousing.message.service.MessageService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageService messageService;

    @Test
    void shouldCreateMessage() throws Exception {
        // Arrange
        MessageResponse response = new MessageResponse(
                100L,
                1L,
                MessageRole.USER,
                "Hello AI",
                Instant.parse("2026-11-24T10:00:00Z")
        );

        when(messageService.create(eq(1L), any(CreateMessageRequest.class)))
                .thenReturn(response);

        CreateMessageRequest request =
                new CreateMessageRequest("Hello AI");

        // Act & Assert
        mockMvc.perform(post("/conversations/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.conversationId").value(1))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.content").value("Hello AI"));

        verify(messageService).create(eq(1L), any(CreateMessageRequest.class));
    }

    @Test
    void shouldRejectBlankMessage() throws Exception {
        // Arrange
        String request = """
                {
                    "content": "   "
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/conversations/1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(messageService);
    }

    @Test
    void shouldReturnMessageHistory() throws Exception {
        // Arrange
        List<MessageResponse> messages = List.of(
                new MessageResponse(
                        1L,
                        1L,
                        MessageRole.USER,
                        "Hello",
                        Instant.parse("2026-11-24T10:00:00Z")
                ),
                new MessageResponse(
                        2L,
                        1L,
                        MessageRole.ASSISTANT,
                        "Hello! How can I help?",
                        Instant.parse("2026-11-24T10:01:00Z")
                )
        );

        when(messageService.findByConversationId(1L))
                .thenReturn(messages);

        // Act & Assert
        mockMvc.perform(get("/conversations/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].role").value("USER"))
                .andExpect(jsonPath("$.data[1].role").value("ASSISTANT"));

        verify(messageService).findByConversationId(1L);
    }

    @Test
    void shouldReturnEmptyMessageHistory() throws Exception {
        // Arrange
        when(messageService.findByConversationId(1L))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/conversations/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenConversationDoesNotExist() throws Exception {
        // Arrange
        when(messageService.findByConversationId(999L))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Conversation not found: 999"
                ));

        // Act & Assert
        mockMvc.perform(get("/conversations/999/messages"))
                .andExpect(status().isNotFound());
    }
}
