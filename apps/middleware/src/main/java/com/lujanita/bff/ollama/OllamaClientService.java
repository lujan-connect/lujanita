package com.lujanita.bff.ollama;

import com.lujanita.bff.config.BffProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaClientService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger log = LoggerFactory.getLogger(OllamaClientService.class);

    @Autowired
    private BffProperties bffProperties;

    public String generate(String model, String prompt) {
        String endpoint = bffProperties.getOllama().getEndpoint();
        String modelName = model != null ? model : bffProperties.getOllama().getModel();
        int timeout = bffProperties.getOllama().getTimeoutMs();

        // Construir prompt final: prefijar systemPrompt si está configurado
        String systemPrompt = bffProperties.getOllama().getSystemPrompt();
        String finalPrompt = (systemPrompt != null && !systemPrompt.isBlank()) ? systemPrompt + "\n\n" + prompt : prompt;

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("prompt", finalPrompt);
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            // Log detallado para troubleshooting
            throw new RuntimeException("Error al conectar con Ollama en " + endpoint + " (modelo: " + modelName + "): " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si el modelo Ollama está disponible localmente.
     * Lanza excepción si no está disponible.
     */
    public void validateModelAvailable() {
        String endpoint = bffProperties.getOllama().getEndpoint();
        String modelName = bffProperties.getOllama().getModel();
        String listUrl = endpoint.replace("/api/generate", "/api/tags");
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(listUrl, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().contains(modelName)) {
                log.info("[Ollama] Modelo '{}' disponible en {}", modelName, listUrl);
            } else {
                throw new IllegalStateException("El modelo '" + modelName + "' no está disponible en Ollama. Descárgalo con: ollama pull " + modelName);
            }
        } catch (RestClientException e) {
            throw new IllegalStateException("No se pudo verificar el modelo Ollama en " + listUrl + ": " + e.getMessage(), e);
        }
    }
}
