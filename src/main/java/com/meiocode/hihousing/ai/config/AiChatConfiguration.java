package com.meiocode.hihousing.ai.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.meiocode.hihousing.ai.service.AiChat;
import com.meiocode.hihousing.ai.tool.TimeTool;

@Configuration
@RequiredArgsConstructor 
public class AiChatConfiguration {

    private final ChatModel chatModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public AiChat aiChat(ChatMemoryProvider chatMemoryProvider) {
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs/rag");
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        return AiServices.builder(AiChat.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore))
                .tools(new TimeTool())
                .build();
    }
}
