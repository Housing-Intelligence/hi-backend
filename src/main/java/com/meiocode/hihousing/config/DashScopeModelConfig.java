package com.meiocode.hihousing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;

@Configuration
@ConditionalOnProperty(
        name = "app.ai.provider",
        havingValue = "qwen"
)
public class DashScopeModelConfig {
    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.community.dashscope.chat-model.model-name}")
    private String modelName;

    @Bean
    public ChatModel chatModel() {
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }
}
