package com.xwm.xiaohongshucopywriteragent.config;

import com.xwm.xiaohongshucopywriteragent.store.MongoChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.cohere.CohereScoringModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Arrays.asList;

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
                .minScore(0.5) // 降低阈值
                .build();

    }

    @Bean
    public ScoringModel cohereScoringModel(){
        String apiKey = "1";
        if(apiKey == null || apiKey.isBlank()){
            throw new IllegalStateException("COHERE_API_KEY 环境变量未配置，无法启用重排序功能");
        }
        return CohereScoringModel.builder()
                .apiKey(apiKey)
                .modelName("rerank-multilingual-v3.0")
                .build();
    }

    @Bean
    public ContentAggregator mingContentAggregator(ScoringModel cohereScoringModel){
        return ReRankingContentAggregator.builder()
                .scoringModel(cohereScoringModel)
                .minScore(0.8)
                .build();
    }

    @Bean
    public ContentInjector mingContentInjector(){
        return DefaultContentInjector.builder()
                .metadataKeysToInclude(List.of("file_name","categoryDescription","categoryPurpose","knowledgeBaseDescription"))
                .build();
    }

    @Bean
    public RetrievalAugmentor mingRetrievalAugmentor(ContentRetriever mingContentRetriever,
                                                     ContentAggregator mingContentAggregator,
                                                     ContentInjector mingContentInjector
    ){
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(mingContentRetriever)
                .contentAggregator(mingContentAggregator)
                .contentInjector(mingContentInjector)
                .build();
    }


}
