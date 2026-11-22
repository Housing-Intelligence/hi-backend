package com.meiocode.hihousing.conversation.service;

import com.meiocode.hihousing.conversation.dto.ConversationResponse;
import com.meiocode.hihousing.conversation.dto.CreateConversationRequest;
import com.meiocode.hihousing.conversation.dto.UpdateConversationRequest;
import com.meiocode.hihousing.conversation.entity.Conversation;
import com.meiocode.hihousing.conversation.repository.ConversationRepository;

import jakarta.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationServiceImpl(
            ConversationRepository conversationRepository
    ) {
        this.conversationRepository = conversationRepository;
    }

    @Override
    public ConversationResponse create(CreateConversationRequest request) {

        Conversation conversation =
                new Conversation(request.title().trim());

        Conversation saved = conversationRepository.save(conversation);

        return toResponse(saved);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ConversationResponse> findAll() {

        return conversationRepository
                .findAllByOrderByUpdatedAtDescIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public ConversationResponse findById(Long id) {

        Conversation conversation = getConversation(id);

        return toResponse(conversation);
    }

    @Override
    public ConversationResponse update(
            Long id,
            UpdateConversationRequest request
    ) {

        Conversation conversation = getConversation(id);

        conversation.updateTitle(request.title().trim());

        return toResponse(conversation);
    }

    @Override
    public void delete(Long id) {

        Conversation conversation = getConversation(id);

        conversationRepository.delete(conversation);
    }

    private Conversation getConversation(Long id) {

        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found: " + id
                ));
    }

    private ConversationResponse toResponse(Conversation conversation) {

        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
