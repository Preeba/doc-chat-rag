package com.example.docchatrag.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RagConfig {

    @Bean
    @Primary
    public EmbeddingStore<TextSegment> embeddingStore() {
        // This will be overridden by the test configuration
        return null;
    }

    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        // This will be overridden by the test configuration
        return null;
    }
} 