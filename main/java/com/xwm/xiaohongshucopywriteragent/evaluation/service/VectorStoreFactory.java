package com.xwm.xiaohongshucopywriteragent.evaluation.service;

import com.xwm.xiaohongshucopywriteragent.evaluation.model.VectorStoreConfig;
import com.xwm.xiaohongshucopywriteragent.evaluation.model.VectorStoreType;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 根据配置动态创建向量库实例
 */
@Component
public class VectorStoreFactory {

    private final EmbeddingModel embeddingModel;
    private final String defaultPineconeApiKey;
    private final String defaultPineconeIndex;
    private final String defaultPineconeNamespace;

    public VectorStoreFactory(EmbeddingModel embeddingModel,
                              @Value("${pinecone.api-key:}") String defaultPineconeApiKey,
                              @Value("${pinecone.index:}") String defaultPineconeIndex,
                              @Value("${pinecone.namespace:}") String defaultPineconeNamespace) {
        this.embeddingModel = embeddingModel;
        this.defaultPineconeApiKey = defaultPineconeApiKey;
        this.defaultPineconeIndex = defaultPineconeIndex;
        this.defaultPineconeNamespace = defaultPineconeNamespace;
    }

    /**
     * Create an {@link EmbeddingStore} implementation based on the provided configuration.
     */
    public EmbeddingStore<TextSegment> create(VectorStoreConfig config) {
        VectorStoreType type = config.getType() == null ? VectorStoreType.PINECONE : config.getType();
        return switch (type) {
            case PINECONE -> createPineconeStore(config);
            case IN_MEMORY -> new InMemoryEmbeddingStore<>();
        };
    }

    private EmbeddingStore<TextSegment> createPineconeStore(VectorStoreConfig config) {
        String apiKey = coalesce(config.getApiKey(), defaultPineconeApiKey);
        String index = coalesce(config.getIndex(), defaultPineconeIndex);
        String namespace = coalesce(config.getNamespace(), defaultPineconeNamespace);

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("Pinecone API key is required for evaluation.");
        }
        if (index == null || index.isBlank()) {
            throw new IllegalArgumentException("Pinecone index is required for evaluation.");
        }
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Pinecone namespace is required for evaluation.");
        }

        PineconeEmbeddingStore.Builder builder = PineconeEmbeddingStore.builder()
                .apiKey(apiKey)
                .index(index)
                .nameSpace(namespace);

        if (config.isCreateIndexIfMissing()) {
            builder.createIndex(PineconeServerlessIndexConfig.builder()
                    .cloud(config.getOptions().getOrDefault("cloud", "AWS"))
                    .region(config.getOptions().getOrDefault("region", "us-east-1"))
                    .dimension(embeddingModel.dimension())
                    .build());
        }

        return builder.build();
    }

    private String coalesce(String preferred, String fallback) {
        return preferred != null && !preferred.isBlank() ? preferred : fallback;
    }
}

