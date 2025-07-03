package com.example.docchatrag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.RetrievalAugmentedGenerator;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {

    @Value("${openai.api-key}")
    private String openaiApiKey;

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    public Mono<String> ask(String question) {
        return Mono.fromCallable(() -> {
            // Create the retrieval augmented generator
            RetrievalAugmentedGenerator rag = RetrievalAugmentedGenerator.builder()
                .contentRetriever(EmbeddingStoreContentRetriever.from(embeddingStore, embeddingModel))
                .chatLanguageModel(createChatModel())
                .build();

            // Generate response
            return rag.generate(question);
        })
        .map(response -> response.content().text())
        .doOnSuccess(response -> log.info("Generated response for question: {}", question));
    }

    public Mono<Void> addDocument(String content) {
        return Mono.fromCallable(() -> {
            Document document = Document.from(content);
            TextSegment segment = TextSegment.from(document.text());
            
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            embeddingStore.add(embedding, segment);
            
            log.info("Added document with {} characters to embedding store", content.length());
            return null;
        })
        .then();
    }

    private ChatLanguageModel createChatModel() {
        return OpenAiChatModel.builder()
            .apiKey(openaiApiKey)
            .modelName("gpt-3.5-turbo")
            .build();
    }
} 