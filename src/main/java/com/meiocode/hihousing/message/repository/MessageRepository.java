
package com.meiocode.hihousing.message.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.meiocode.hihousing.message.entity.Message;

public interface MessageRepository
        extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtAscIdAsc(Long conversationId);

    List<Message> findTop9ByConversationIdOrderByCreatedAtDescIdDesc(
            Long conversationId);
}
