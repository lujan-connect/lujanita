package com.lujanita.bff.ollama;

import com.lujanita.bff.config.BffProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaClientService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private BffProperties bffProperties;

    public String generate(String model, String prompt) {
        String endpoint = bffProperties.getOllama().getEndpoint();
        String modelName = model != null ? model : bffProperties.getOllama().getModel();
        int timeout = bffProperties.getOllama().getTimeoutMs();

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("prompt", prompt);
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
        return response.getBody();
    }
}
