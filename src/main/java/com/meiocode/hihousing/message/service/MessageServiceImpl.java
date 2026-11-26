package com.meiocode.hihousing.message.service;

import com.meiocode.hihousing.conversation.entity.Conversation;
import com.meiocode.hihousing.conversation.repository.ConversationRepository;
import com.meiocode.hihousing.message.dto.CreateMessageRequest;
import com.meiocode.hihousing.message.dto.MessageResponse;
import com.meiocode.hihousing.message.entity.Message;
import com.meiocode.hihousing.message.entity.MessageRole;
import com.meiocode.hihousing.message.repository.MessageRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;

    public MessageServiceImpl(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
    }

    @Override
    @Transactional
    public MessageResponse create(
            Long conversationId,
            CreateMessageRequest request) {

        Conversation conversation = getConversation(conversationId);

        Message message = new Message(
                conversation,
                MessageRole.USER,
                request.content().trim());

        conversation.markUpdated();

        Message savedMessage = messageRepository.save(message);

        return toResponse(savedMessage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> findByConversationId(
            Long conversationId) {

        getConversation(conversationId);

        return messageRepository
                .findByConversationIdOrderByCreatedAtAscIdAsc(conversationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public MessageResponse saveAssistantMessage(
            Long conversationId,
            String content) {

        Conversation conversation = getConversation(conversationId);

        Message message = new Message(
                conversation,
                MessageRole.ASSISTANT,
                content.trim());

        Message savedMessage = messageRepository.save(message);

        conversation.markUpdated();

        return toResponse(savedMessage);
    }

    private Conversation getConversation(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found: " + id));
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt());
    }
}
