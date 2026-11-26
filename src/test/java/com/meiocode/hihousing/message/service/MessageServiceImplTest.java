
package com.meiocode.hihousing.message.service;

import com.meiocode.hihousing.conversation.entity.Conversation;
import com.meiocode.hihousing.conversation.repository.ConversationRepository;
import com.meiocode.hihousing.message.dto.CreateMessageRequest;
import com.meiocode.hihousing.message.dto.MessageResponse;
import com.meiocode.hihousing.message.entity.Message;
import com.meiocode.hihousing.message.entity.MessageRole;
import com.meiocode.hihousing.message.repository.MessageRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Conversation createConversation(Long id) {
        Conversation conversation = new Conversation("Test conversation");
        ReflectionTestUtils.setField(conversation, "id", id);
        return conversation;
    }

    private Message createMessage(
            Conversation conversation,
            MessageRole role,
            String content,
            Long id,
            Instant createdAt
    ) {
        Message message = new Message(conversation, role, content);
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", createdAt);
        return message;
    }

    @Test
    void shouldCreateUserMessage() {
        // Arrange
        Long conversationId = 1L;
        Conversation conversation = createConversation(conversationId);

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        Instant createdAt = Instant.now();

        when(messageRepository.save(any(Message.class)))
                .thenAnswer(invocation -> {
                    Message message = invocation.getArgument(0);
                    ReflectionTestUtils.setField(message, "id", 100L);
                    ReflectionTestUtils.setField(message, "createdAt", createdAt);
                    return message;
                });

        // Act
        MessageResponse response = messageService.create(
                conversationId,
                new CreateMessageRequest("  Hello AI  ")
        );

        // Assert
        assertEquals(100L, response.id());
        assertEquals(conversationId, response.conversationId());
        assertEquals(MessageRole.USER, response.role());
        assertEquals("Hello AI", response.content());
        assertEquals(createdAt, response.createdAt());

        assertNotNull(conversation.getUpdatedAt());

        verify(conversationRepository).findById(conversationId);
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void shouldThrowNotFoundWhenCreatingMessageForMissingConversation() {
        // Arrange
        Long conversationId = 999L;

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> messageService.create(
                        conversationId,
                        new CreateMessageRequest("Hello")
                )
        );

        // Assert
        assertEquals(404, exception.getStatusCode().value());

        verify(messageRepository, never()).save(any());
    }

    @Test
    void shouldReturnMessagesInChronologicalOrder() {
        // Arrange
        Long conversationId = 1L;
        Conversation conversation = createConversation(conversationId);

        Instant firstTime = Instant.parse("2026-11-24T10:00:00Z");
        Instant secondTime = Instant.parse("2026-11-24T10:01:00Z");

        Message userMessage = createMessage(
                conversation,
                MessageRole.USER,
                "Hello",
                1L,
                firstTime
        );

        Message assistantMessage = createMessage(
                conversation,
                MessageRole.ASSISTANT,
                "Hello! How can I help?",
                2L,
                secondTime
        );

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        when(messageRepository
                .findByConversationIdOrderByCreatedAtAscIdAsc(conversationId))
                .thenReturn(List.of(userMessage, assistantMessage));

        // Act
        List<MessageResponse> responses =
                messageService.findByConversationId(conversationId);

        // Assert
        assertEquals(2, responses.size());

        assertEquals(MessageRole.USER, responses.get(0).role());
        assertEquals("Hello", responses.get(0).content());

        assertEquals(MessageRole.ASSISTANT, responses.get(1).role());
        assertEquals("Hello! How can I help?", responses.get(1).content());

        verify(messageRepository)
                .findByConversationIdOrderByCreatedAtAscIdAsc(conversationId);
    }

    @Test
    void shouldReturnEmptyListWhenConversationHasNoMessages() {
        // Arrange
        Long conversationId = 1L;
        Conversation conversation = createConversation(conversationId);

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.of(conversation));

        when(messageRepository
                .findByConversationIdOrderByCreatedAtAscIdAsc(conversationId))
                .thenReturn(List.of());

        // Act
        List<MessageResponse> responses =
                messageService.findByConversationId(conversationId);

        // Assert
        assertTrue(responses.isEmpty());
    }

    @Test
    void shouldThrowNotFoundWhenGettingMessagesForMissingConversation() {
        // Arrange
        Long conversationId = 999L;

        when(conversationRepository.findById(conversationId))
                .thenReturn(Optional.empty());

        // Act
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> messageService.findByConversationId(conversationId)
        );

        // Assert
        assertEquals(404, exception.getStatusCode().value());

        verify(messageRepository, never())
                .findByConversationIdOrderByCreatedAtAscIdAsc(any());
    }
}
