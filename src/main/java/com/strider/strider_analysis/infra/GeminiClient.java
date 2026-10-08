package com.strider.strider_analysis.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
public class GeminiClient {

    private final WebClient webClient;
    private final Semaphore semaphore = new Semaphore(1);

    @Value("${openrouter.api-key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String primaryModel;

    @Value("#{'${openrouter.fallback-models}'.split(',')}")
    private List<String> fallbackModels;

    @Value("${openrouter.timeout-seconds}")
    private int timeoutSeconds;

    public GeminiClient(@Value("${openrouter.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String generate(String systemPrompt, String userPrompt) {
        try {
            log.info("[OpenRouter] 요청 대기 중 (동시 호출 직렬화)");
            semaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OpenRouter 요청 대기 중 인터럽트", e);
        }

        try {
            List<String> models = new ArrayList<>();
            models.add(primaryModel);
            models.addAll(fallbackModels);

            Exception lastException = null;
            for (String model : models) {
                for (int attempt = 1; attempt <= 2; attempt++) {
                    try {
                        log.info("[OpenRouter] 모델 시도: {} (attempt {})", model, attempt);
                        String result = callModel(model, systemPrompt, userPrompt);
                        if (!model.equals(primaryModel)) {
                            log.warn("[OpenRouter] 폴백 모델 사용됨: {}", model);
                        }
                        return result;
                    } catch (Exception e) {
                        lastException = e;
                        boolean isRateLimit = e.getMessage() != null && e.getMessage().contains("429");
                        if (isRateLimit && attempt < 2) {
                            log.warn("[OpenRouter] 429 Rate Limit - 15초 후 재시도: {}", model);
                            try { Thread.sleep(15000L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        } else {
                            log.warn("[OpenRouter] 모델 실패: {} - {}", model, e.getMessage());
                            break;
                        }
                    }
                }
            }

            throw new RuntimeException("모든 OpenRouter 모델 호출 실패: " + lastException.getMessage(), lastException);
        } finally {
            semaphore.release();
        }
    }

    private String callModel(String model, String systemPrompt, String userPrompt) {
        var body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        Map<?, ?> response = webClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

        return parseText(response);
    }

    private String parseText(Map<?, ?> response) {
        var choices = (List<?>) response.get("choices");
        var message = (Map<?, ?>) ((Map<?, ?>) choices.get(0)).get("message");
        return message.get("content").toString();
    }
}
