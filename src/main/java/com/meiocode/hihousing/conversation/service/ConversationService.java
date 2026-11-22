package com.meiocode.hihousing.conversation.service;

import com.meiocode.hihousing.conversation.dto.ConversationResponse;
import com.meiocode.hihousing.conversation.dto.CreateConversationRequest;
import com.meiocode.hihousing.conversation.dto.UpdateConversationRequest;

import java.util.List;

public interface ConversationService {

    ConversationResponse create(CreateConversationRequest request);

    List<ConversationResponse> findAll();

    ConversationResponse findById(Long id);

    ConversationResponse update(
            Long id,
            UpdateConversationRequest request
    );

    void delete(Long id);
}
