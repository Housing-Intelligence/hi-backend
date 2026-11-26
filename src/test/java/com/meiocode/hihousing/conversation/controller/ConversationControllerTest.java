package com.meiocode.hihousing.conversation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.meiocode.hihousing.common.exception.GlobalExceptionHandler;
import com.meiocode.hihousing.conversation.dto.ConversationResponse;
import com.meiocode.hihousing.conversation.dto.CreateConversationRequest;
import com.meiocode.hihousing.conversation.dto.UpdateConversationRequest;
import com.meiocode.hihousing.conversation.service.ConversationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConversationController.class)
@Import(GlobalExceptionHandler.class)
class ConversationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConversationService conversationService;

    private ConversationResponse createResponse() {

        Instant now = Instant.parse("2026-11-22T10:00:00Z");

        return new ConversationResponse(
                1L,
                "Netherlands Housing Search",
                now,
                now
        );
    }

    // Create conversation
    @Test
    void shouldCreateConversation() throws Exception {

        when(conversationService.create(any(CreateConversationRequest.class)))
                .thenReturn(createResponse());

        CreateConversationRequest request =
                new CreateConversationRequest(
                        "Netherlands Housing Search"
                );

        mockMvc.perform(post("/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Conversation created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title")
                        .value("Netherlands Housing Search"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(conversationService).create(any(CreateConversationRequest.class));
    }

    // Get all conversations
    @Test
    void shouldFindAllConversations() throws Exception {

        ConversationResponse response = createResponse();

        when(conversationService.findAll())
                .thenReturn(List.of(response));

        mockMvc.perform(get("/conversations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title")
                        .value("Netherlands Housing Search"));

        verify(conversationService).findAll();
    }

    // Get conversation by ID
    @Test
    void shouldFindConversationById() throws Exception {

        when(conversationService.findById(1L))
                .thenReturn(createResponse());

        mockMvc.perform(get("/conversations/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title")
                        .value("Netherlands Housing Search"));

        verify(conversationService).findById(1L);
    }

    // Update conversation
    @Test
    void shouldUpdateConversation() throws Exception {

        Instant now = Instant.parse("2026-11-22T10:00:00Z");

        ConversationResponse updatedResponse =
                new ConversationResponse(
                        1L,
                        "Amsterdam Housing Search",
                        now,
                        now
                );

        when(conversationService.update(
                eq(1L),
                any(UpdateConversationRequest.class)
        )).thenReturn(updatedResponse);

        UpdateConversationRequest request =
                new UpdateConversationRequest(
                        "Amsterdam Housing Search"
                );

        mockMvc.perform(put("/conversations/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title")
                        .value("Amsterdam Housing Search"));

        verify(conversationService).update(
                eq(1L),
                any(UpdateConversationRequest.class)
        );
    }

    // Delete conversation
    @Test
    void shouldDeleteConversation() throws Exception {

        doNothing().when(conversationService).delete(1L);

        mockMvc.perform(delete("/conversations/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Conversation deleted successfully"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));

        verify(conversationService).delete(1L);
    }

    // Validation: blank title
    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {

        String invalidRequest = """
                {
                    "title": "   "
                }
                """;

        mockMvc.perform(post("/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("title: Title cannot be blank"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));

        verifyNoInteractions(conversationService);
    }

    // Not found: GET
    @Test
    void shouldReturnNotFoundWhenConversationDoesNotExist() throws Exception {

        when(conversationService.findById(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found: 999"
                ));

        mockMvc.perform(get("/conversations/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Conversation not found: 999"))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }
}
