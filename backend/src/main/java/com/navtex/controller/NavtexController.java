package com.navtex.controller;

import com.navtex.model.NavtexMessage;
import com.navtex.service.NavtexService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NavtexController {

    private final NavtexService navtexService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${anthropic.api.key:}")
    private String anthropicApiKey;

    public NavtexController(NavtexService navtexService) {
        this.navtexService = navtexService;
    }

    @GetMapping("/messages")
    public ResponseEntity<List<NavtexMessage>> getAllMessages() {
        return ResponseEntity.ok(navtexService.getAllMessages());
    }

    @GetMapping("/messages/country/{country}")
    public ResponseEntity<List<NavtexMessage>> getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(navtexService.getMessagesByCountry(country));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "navtex-api"));
    }

    // POST /api/summarize — proxies to Claude API
    @PostMapping("/summarize")
    public ResponseEntity<Map<String, String>> summarize(@RequestBody Map<String, String> body) {
        String raw = body.get("raw");
        if (raw == null || raw.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "raw mesaj boş"));
        }

        String key = anthropicApiKey;
        if (key == null || key.isBlank()) {
            return ResponseEntity.status(500).body(Map.of("error", "ANTHROPIC_API_KEY ayarlanmamış"));
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", key);
            headers.set("anthropic-version", "2023-06-01");

            Map<String, Object> requestBody = Map.of(
                "model", "claude-sonnet-4-20250514",
                "max_tokens", 1000,
                "system", "Sen bir denizcilik uzmanısın. Sana ham NAVTEX mesajı gelecek. Türkçe 2-3 cümleyle özet yaz: mesaj türü, istasyon, kaptan için kritik bilgi. Kısaltmaları açıkla. Sadece özeti yaz.",
                "messages", List.of(Map.of("role", "user", "content", "NAVTEX mesajı:\n" + raw))
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.anthropic.com/v1/messages", request, Map.class
            );

            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
            String summary = content.stream()
                .map(c -> (String) c.get("text"))
                .filter(t -> t != null)
                .reduce("", String::concat);

            return ResponseEntity.ok(Map.of("summary", summary));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Claude API hatası: " + e.getMessage()));
        }
    }
}
