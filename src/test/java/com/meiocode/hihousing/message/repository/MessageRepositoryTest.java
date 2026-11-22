
package com.meiocode.hihousing.message.repository;

import com.meiocode.hihousing.conversation.entity.Conversation;
import com.meiocode.hihousing.conversation.repository.ConversationRepository;
import com.meiocode.hihousing.message.entity.Message;
import com.meiocode.hihousing.message.entity.MessageRole;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Test
    void shouldSaveAndFindMessagesByConversationIdInOrder() {

        // 1. Create and save conversation
        Conversation conversation =
                conversationRepository.save(
                        new Conversation("Test Conversation")
                );

        // 2. Create and save messages
        Message userMessage = new Message(
                conversation,
                MessageRole.USER,
                "Hello"
        );

        Message assistantMessage = new Message(
                conversation,
                MessageRole.ASSISTANT,
                "Hi, how can I help you?"
        );

        messageRepository.save(userMessage);
        messageRepository.save(assistantMessage);

        // 3. Flush changes to database
        entityManager.flush();

        // 4. Query messages by conversation ID
        List<Message> messages =
                messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(
                        conversation.getId()
                );

        // 5. Verify results
        assertThat(messages).hasSize(2);

        assertThat(messages.get(0).getRole())
                .isEqualTo(MessageRole.USER);

        assertThat(messages.get(0).getContent())
                .isEqualTo("Hello");

        assertThat(messages.get(1).getRole())
                .isEqualTo(MessageRole.ASSISTANT);

        assertThat(messages.get(1).getContent())
                .isEqualTo("Hi, how can I help you?");
    }


    @Test
    void shouldDeleteMessagesWhenConversationIsDeleted() {

        // 1. Create and save conversation
        Conversation conversation =
                conversationRepository.save(
                        new Conversation("Test Conversation")
                );

        Long conversationId = conversation.getId();

        // 2. Create and save messages
        messageRepository.save(
                new Message(
                        conversation,
                        MessageRole.USER,
                        "Hello"
                )
        );

        messageRepository.save(
                new Message(
                        conversation,
                        MessageRole.ASSISTANT,
                        "Hi"
                )
        );

        // 3. Flush inserts to database
        entityManager.flush();

        // 4. Clear persistence context
        entityManager.clear();

        // 5. Delete conversation
        conversationRepository.deleteById(conversationId);

        // 6. Flush deletion
        entityManager.flush();

        // 7. Clear persistence context again
        entityManager.clear();

        // 8. Verify messages are also deleted
        List<Message> messages =
                messageRepository.findByConversationIdOrderByCreatedAtAscIdAsc(
                        conversationId
                );

        assertThat(messages).isEmpty();
    }
}
