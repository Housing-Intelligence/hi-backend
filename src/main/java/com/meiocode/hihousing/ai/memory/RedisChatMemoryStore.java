
package com.meiocode.hihousing.ai.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final Duration ttl;

    public RedisChatMemoryStore(
            StringRedisTemplate redisTemplate,
            @Value("${app.ai.memory.key-prefix:hi:chat-memory:}")
            String keyPrefix,
            @Value("${app.ai.memory.ttl-seconds:86400}")
            long ttlSeconds
    ) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Chat memory TTL must be greater than zero"
            );
        }

        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    /**
     * Retrieve chat messages from Redis.
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {

        String key = buildKey(memoryId);

        String json = redisTemplate.opsForValue().get(key);

        if (json == null || json.isBlank()) {
            return List.of();
        }

        return ChatMessageDeserializer.messagesFromJson(json);
    }

    /**
     * Update the complete current state of ChatMemory.
     */
    @Override
    public void updateMessages(
            Object memoryId,
            List<ChatMessage> messages
    ) {
        String key = buildKey(memoryId);

        String json = ChatMessageSerializer.messagesToJson(messages);

        redisTemplate.opsForValue().set(key, json, ttl);
    }

    /**
     * Delete chat memory for a conversation.
     */
    @Override
    public void deleteMessages(Object memoryId) {

        String key = buildKey(memoryId);

        redisTemplate.delete(key);
    }

    private String buildKey(Object memoryId) {

        Objects.requireNonNull(
                memoryId,
                "Memory ID cannot be null"
        );

        return keyPrefix + memoryId;
    }
}
