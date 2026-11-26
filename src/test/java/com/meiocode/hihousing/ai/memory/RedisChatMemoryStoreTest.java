
package com.meiocode.hihousing.ai.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisChatMemoryStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisChatMemoryStore redisChatMemoryStore;

    @BeforeEach
    void setUp() {
        redisChatMemoryStore = new RedisChatMemoryStore(
                redisTemplate,
                "hi:chat-memory:",
                86400
        );
    }

    @Test
    void shouldReturnEmptyListWhenMemoryDoesNotExist() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("hi:chat-memory:1001"))
                .thenReturn(null);

        List<ChatMessage> result =
                redisChatMemoryStore.getMessages(1001L);

        assertTrue(result.isEmpty());

        verify(valueOperations).get("hi:chat-memory:1001");
    }

    @Test
    void shouldDeserializeMessagesFromRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        List<ChatMessage> messages = List.of(
                UserMessage.from("Hello AI")
        );

        String json = ChatMessageSerializer.messagesToJson(messages);

        when(valueOperations.get("hi:chat-memory:1001"))
                .thenReturn(json);

        List<ChatMessage> result =
                redisChatMemoryStore.getMessages(1001L);

        assertEquals(1, result.size());
        assertEquals(messages.get(0), result.get(0));

        verify(valueOperations).get("hi:chat-memory:1001");
    }

    @Test
    void shouldSerializeAndSaveMessagesToRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        List<ChatMessage> messages = List.of(
                UserMessage.from("Hello AI")
        );

        String expectedJson =
                ChatMessageSerializer.messagesToJson(messages);

        redisChatMemoryStore.updateMessages(1001L, messages);

        verify(valueOperations).set(
                "hi:chat-memory:1001",
                expectedJson,
                Duration.ofHours(24)
        );
    }

    @Test
    void shouldDeleteMemoryFromRedis() {
        redisChatMemoryStore.deleteMessages(1001L);

        verify(redisTemplate).delete("hi:chat-memory:1001");
    }
}
