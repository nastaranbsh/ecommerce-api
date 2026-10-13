package com.nbsh.commerceapi.common.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    private final String serviceName;

    public HealthController(
            @Value("${spring.application.name}") String serviceName
    ) {
        this.serviceName = serviceName;
    }

    @GetMapping
    public Map<String, Object> health() {

        return Map.of(
                "status", "UP",
                "service", serviceName,
                "timestamp", Instant.now()
        );
    }
}