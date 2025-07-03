package com.example.docchatrag.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/actuator")
public class HealthController {

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        Map<String, String> healthStatus = Map.of("status", "UP");
        return Mono.just(ResponseEntity.ok(healthStatus));
    }
} 