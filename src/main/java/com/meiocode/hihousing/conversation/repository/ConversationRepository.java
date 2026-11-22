
package com.meiocode.hihousing.conversation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.meiocode.hihousing.conversation.entity.Conversation;

public interface ConversationRepository
        extends JpaRepository<Conversation, Long> {

    List<Conversation> findAllByOrderByUpdatedAtDescIdDesc();
}
