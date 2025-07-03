package com.example.docchatrag.controller;

import com.example.docchatrag.service.DocumentIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentIngestService documentIngestService;

    @PostMapping("/ingest")
    public Mono<ResponseEntity<Map<String, Object>>> ingestDocuments(@RequestParam String directory) {
        Path dirPath = Paths.get(directory);
        
        return documentIngestService.ingest(dirPath)
            .map(count -> {
                log.info("Successfully ingested {} documents from directory: {}", count, directory);
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "documentsIngested", count,
                    "directory", directory
                ));
            })
            .onErrorResume(e -> {
                log.error("Error ingesting documents from directory: {}", directory, e);
                return Mono.just(ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage(),
                    "directory", directory
                )));
            });
    }
} 