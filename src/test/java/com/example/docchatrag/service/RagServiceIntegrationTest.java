package com.example.docchatrag.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class RagServiceIntegrationTest {

    @Container
    static GenericContainer<?> qdrantContainer = new GenericContainer<>(
        DockerImageName.parse("qdrant/qdrant:latest")
    )
        .withExposedPorts(6334)
        .withEnv("QDRANT__SERVICE__HTTP_PORT", "6334")
        .withEnv("QDRANT__SERVICE__GRPC_PORT", "6335");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("qdrant.host", qdrantContainer::getHost);
        registry.add("qdrant.port", () -> qdrantContainer.getMappedPort(6334));
        registry.add("openai.api-key", () -> "test-api-key");
    }

    @Autowired
    private RagService ragService;

    @MockBean
    private EmbeddingModel embeddingModel;

    @MockBean
    private EmbeddingStore<TextSegment> embeddingStore;

    @BeforeEach
    void setUp() {
        // Mock the embedding model to return fixed vectors
        List<Float> fixedVector = List.of(0.1f, 0.2f, 0.3f, 0.4f, 0.5f);
        Embedding mockEmbedding = Embedding.from(fixedVector);
        
        when(embeddingModel.embed(anyString())).thenReturn(mockEmbedding);
        when(embeddingModel.embed(any(TextSegment.class))).thenReturn(mockEmbedding);
        
        // Mock the embedding store to return a mock match
        dev.langchain4j.store.embedding.EmbeddingMatch<TextSegment> mockMatch = 
            dev.langchain4j.store.embedding.EmbeddingMatch.from(
                0.95,
                "test-id",
                TextSegment.from("This is a test document about hello world.")
            );
        
        when(embeddingStore.findRelevant(any(Embedding.class), any(Integer.class), any(Double.class)))
            .thenReturn(List.of(mockMatch));
    }

    @Test
    void testRagAskReturnsNonEmptyString() {
        // Given
        String question = "hello";
        
        // When
        Mono<String> result = ragService.ask(question);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response).isNotEmpty();
                assertThat(response).isNotBlank();
            })
            .verifyComplete();
    }

    @Test
    void testAddDocument() {
        // Given
        String documentContent = "This is a test document for RAG testing.";
        
        // When
        Mono<Void> result = ragService.addDocument(documentContent);
        
        // Then
        StepVerifier.create(result)
            .verifyComplete();
    }

    @Test
    void testRagAskWithMockedResponse() {
        // Given
        String question = "hello";
        
        // When
        Mono<String> result = ragService.ask(question);
        
        // Then
        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response).isNotNull();
                assertThat(response).isNotEmpty();
                // The response should contain some text from the mock match
                assertThat(response).contains("test document");
            })
            .verifyComplete();
    }
} 