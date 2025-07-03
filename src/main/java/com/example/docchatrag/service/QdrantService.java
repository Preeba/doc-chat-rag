package com.example.docchatrag.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QdrantService {

    @Value("${qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${qdrant.port:6334}")
    private int qdrantPort;

    @Value("${qdrant.collection-name:doc_chunks}")
    private String collectionName;

    public Mono<Void> upsertSegment(TextSegment segment, Embedding embedding) {
        return Mono.fromCallable(() -> {
            // Create a unique ID for the segment
            String segmentId = UUID.randomUUID().toString();
            
            // Convert embedding to float array
            List<Float> vector = embedding.vectorAsList();
            
            // TODO: Implement actual Qdrant gRPC client call
            // For now, we'll log the operation
            log.info("Upserting segment {} with vector size {} to collection {}", 
                segmentId, vector.size(), collectionName);
            
            return null;
        })
        .then();
    }

    public Mono<Void> createCollection() {
        return Mono.fromCallable(() -> {
            log.info("Creating collection: {}", collectionName);
            // TODO: Implement collection creation via gRPC
            return null;
        })
        .then();
    }

    public Mono<Void> deleteCollection() {
        return Mono.fromCallable(() -> {
            log.info("Deleting collection: {}", collectionName);
            // TODO: Implement collection deletion via gRPC
            return null;
        })
        .then();
    }
} 