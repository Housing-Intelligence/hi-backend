package com.meiocode.hihousing.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface AiChat {

    @SystemMessage(fromResource = "docs/system-prompt/ai-chat.txt")
    String chat(@MemoryId String memoryId, @UserMessage String prompt);
}