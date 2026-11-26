package com.meiocode.hihousing.conversation.service;

import com.meiocode.hihousing.conversation.dto.ConversationResponse;
import com.meiocode.hihousing.conversation.dto.CreateConversationRequest;
import com.meiocode.hihousing.conversation.dto.UpdateConversationRequest;
import com.meiocode.hihousing.conversation.entity.Conversation;
import com.meiocode.hihousing.conversation.repository.ConversationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversationServiceImplTest {

    @Mock
    private ConversationRepository conversationRepository;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private Conversation conversation;

    @BeforeEach
    void setUp() {
        conversation = createConversation(
                1L,
                "Netherlands Housing Search"
        );
    }

    // Create conversation
    @Test
    void shouldCreateConversation() {

        when(conversationRepository.save(any(Conversation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateConversationRequest request =
                new CreateConversationRequest(
                        "  Netherlands Housing Search  "
                );

        ConversationResponse response =
                conversationService.create(request);

        ArgumentCaptor<Conversation> captor =
                ArgumentCaptor.forClass(Conversation.class);

        verify(conversationRepository).save(captor.capture());

        assertThat(captor.getValue().getTitle())
                .isEqualTo("Netherlands Housing Search");

        assertThat(response.title())
                .isEqualTo("Netherlands Housing Search");
    }

    // Find all conversations
    @Test
    void shouldFindAllConversations() {

        Conversation secondConversation =
                createConversation(2L, "Amsterdam Housing");

        when(conversationRepository.findAllByOrderByUpdatedAtDescIdDesc())
                .thenReturn(List.of(conversation, secondConversation));

        List<ConversationResponse> responses =
                conversationService.findAll();

        assertThat(responses).hasSize(2);

        assertThat(responses.get(0).title())
                .isEqualTo("Netherlands Housing Search");

        assertThat(responses.get(1).title())
                .isEqualTo("Amsterdam Housing");
    }

    // Find conversation by ID
    @Test
    void shouldFindConversationById() {

        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        ConversationResponse response =
                conversationService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);

        assertThat(response.title())
                .isEqualTo("Netherlands Housing Search");
    }

    // Find non-existing conversation
    @Test
    void shouldThrowExceptionWhenConversationDoesNotExist() {

        when(conversationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> conversationService.findById(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Conversation not found: 999")
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    // Update conversation title
    @Test
    void shouldUpdateConversationTitle() {

        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        UpdateConversationRequest request =
                new UpdateConversationRequest("Updated Housing Search");

        ConversationResponse response =
                conversationService.update(1L, request);

        assertThat(response.title())
                .isEqualTo("Updated Housing Search");

        assertThat(conversation.getTitle())
                .isEqualTo("Updated Housing Search");

        verify(conversationRepository, never())
                .save(any(Conversation.class));
    }

    // Update non-existing conversation
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingConversation() {

        when(conversationRepository.findById(999L))
                .thenReturn(Optional.empty());

        UpdateConversationRequest request =
                new UpdateConversationRequest("New Title");

        assertThatThrownBy(
                () -> conversationService.update(999L, request)
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);
                });

        verify(conversationRepository, never())
                .delete(any(Conversation.class));
    }

    // Delete conversation
    @Test
    void shouldDeleteConversation() {

        when(conversationRepository.findById(1L))
                .thenReturn(Optional.of(conversation));

        conversationService.delete(1L);

        verify(conversationRepository).delete(conversation);
    }

    // Delete non-existing conversation
    @Test
    void shouldThrowExceptionWhenDeletingNonExistingConversation() {

        when(conversationRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> conversationService.delete(999L)
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException ex =
                            (ResponseStatusException) exception;

                    assertThat(ex.getStatusCode())
                            .isEqualTo(HttpStatus.NOT_FOUND);
                });

        verify(conversationRepository, never())
                .delete(any(Conversation.class));
    }

    // Test helper
    private Conversation createConversation(Long id, String title) {

        Conversation conversation = new Conversation(title);

        ReflectionTestUtils.setField(
                conversation,
                "id",
                id
        );

        return conversation;
    }
}
