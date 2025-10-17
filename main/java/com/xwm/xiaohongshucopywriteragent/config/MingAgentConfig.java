package com.xwm.xiaohongshucopywriteragent.config;

import com.xwm.xiaohongshucopywriteragent.store.MongoChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MingAgentConfig {

    @Autowired
    private MongoChatMemoryStore mongoChatMemoryStore;
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore embeddingStore;

    @Bean
    public ChatMemoryProvider mingChatMemoryProvider(){
        return memoryID ->  MessageWindowChatMemory
                .builder()
                .id(memoryID)
                .maxMessages(100)
                .chatMemoryStore(mongoChatMemoryStore)
                .build();
    }

    /**
     *
     */
    @Bean
    public ContentRetriever mingContentRetriever(){

        // 基础检索器
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(10) // 检索更多结果用于重排序
                .minScore(0.8) // 降低阈值
                .build();

    }


}
