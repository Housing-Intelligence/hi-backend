
package com.meiocode.hihousing.message.service;

import com.meiocode.hihousing.message.dto.CreateMessageRequest;
import com.meiocode.hihousing.message.dto.MessageResponse;

import java.util.List;

public interface MessageService {

        MessageResponse create(
                        Long conversationId,
                        CreateMessageRequest request);

        List<MessageResponse> findByConversationId(
                        Long conversationId);

        MessageResponse saveAssistantMessage(
                        Long conversationId,
                        String content);
}
