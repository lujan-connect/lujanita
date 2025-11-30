package com.lujanita.bff.mcp;

import com.lujanita.bff.config.BffProperties;
import com.lujanita.bff.model.dto.McpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;

@Service
public class McpClientService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BffProperties bffProperties;

    public McpResponse callMcp(String method, Map<String, Object> params, Map<String, String> headers) {
        // Normalizar headers a minúsculas por seguridad
        Map<String, String> normHeaders = new HashMap<>();
        if (headers != null) headers.forEach((k, v) -> normHeaders.put(k.toLowerCase(), v));

        String endpoint = bffProperties.getMcp().getEndpoint();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        Map<String, Object> body = Map.of(
            "method", method,
            "params", params
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
        try {
            return objectMapper.readValue(response.getBody(), McpResponse.class);
        } catch (Exception e) {
            McpResponse error = new McpResponse();
            error.setCode("MW005");
            error.setMessage("Error deserializando respuesta MCP: " + e.getMessage());
            return error;
        }
    }
}
