
package com.meiocode.hihousing.conversation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.meiocode.hihousing.conversation.entity.Conversation;

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Test
    void shouldSaveAndFindConversation() {
        Conversation conversation =
            new Conversation("Find a house in Eindhoven");

        Conversation saved =
            conversationRepository.saveAndFlush(conversation);

        assertThat(saved.getId()).isNotNull();

        Conversation found = conversationRepository
            .findById(saved.getId())
            .orElseThrow();

        assertThat(found.getTitle())
            .isEqualTo("Find a house in Eindhoven");
    }

    @Test
    void shouldDeleteConversation() {
        Conversation conversation =
            conversationRepository.saveAndFlush(
                new Conversation("Test conversation")
            );

        Long id = conversation.getId();

        conversationRepository.deleteById(id);
        conversationRepository.flush();

        assertThat(conversationRepository.existsById(id)).isFalse();
    }
}
