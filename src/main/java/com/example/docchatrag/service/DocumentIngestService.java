package com.example.docchatrag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentIngestService {

    @Value("${openai.api-key}")
    private String openaiApiKey;

    @Value("${openai.model:text-embedding-3-small}")
    private String embeddingModel;

    private final QdrantService qdrantService;

    public Mono<Integer> ingest(Path dir) {
        return Flux.fromStream(() -> {
            try {
                return Files.walk(dir)
                    .filter(path -> path.toString().endsWith(".md"))
                    .filter(Files::isRegularFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to walk directory: " + dir, e);
            }
        })
        .flatMap(this::processMarkdownFile)
        .reduce(0, Integer::sum)
        .doOnSuccess(count -> log.info("Ingested {} documents from directory: {}", count, dir));
    }

    private Mono<Integer> processMarkdownFile(Path filePath) {
        return Mono.fromCallable(() -> {
            String content = Files.readString(filePath);
            return Document.from(content, filePath.toString());
        })
        .flatMap(document -> {
            DocumentSplitter splitter = DocumentSplitters.recursive(1000, 200);
            List<TextSegment> segments = splitter.split(document);
            
            log.info("Split document {} into {} segments", filePath, segments.size());
            
            return Flux.fromIterable(segments)
                .flatMap(this::embedAndStoreSegment)
                .reduce(0, (count, ignored) -> count + 1);
        })
        .onErrorResume(e -> {
            log.error("Error processing file: {}", filePath, e);
            return Mono.just(0);
        });
    }

    private Mono<Void> embedAndStoreSegment(TextSegment segment) {
        return Mono.fromCallable(() -> {
            EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(openaiApiKey)
                .modelName(this.embeddingModel)
                .build();
            
            return embeddingModel.embed(segment.text()).content();
        })
        .flatMap(embedding -> qdrantService.upsertSegment(segment, embedding))
        .then();
    }
} 