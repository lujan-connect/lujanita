package com.lujanita.bff.mcp;

import com.lujanita.bff.config.BffProperties;
import com.lujanita.bff.model.dto.McpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Optional;

@Slf4j
@Service
public class McpClientService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BffProperties bffProperties;
    @Autowired
    private McpSessionService mcpSessionService;

    public McpResponse callMcp(String method, Map<String, Object> params, Map<String, String> headers) {
        String endpoint = bffProperties.getMcp().getEndpoint();
        Map<String, String> effectiveHeaders = new HashMap<>();
        if (headers != null) effectiveHeaders.putAll(headers);

        String authToken = resolveApiKey(headers);
        if (authToken != null && !authToken.isBlank()) {
            effectiveHeaders.put("Authorization", "Bearer " + authToken);
            effectiveHeaders.put("X-Api-Key", authToken);
        }
        String transport = sanitizeTransport(bffProperties.getMcp().getTransport());
        effectiveHeaders.put("MCP-Transport", transport);
        String roleHeader = headers == null ? null : (headers.get("X-Role") != null ? headers.get("X-Role") : headers.get("x-role"));
        if (roleHeader != null && !roleHeader.isBlank()) {
            effectiveHeaders.put("X-Role", roleHeader);
        }
        String profileHeader = headers == null ? null : (headers.get("X-Profile") != null ? headers.get("X-Profile") : headers.get("x-profile"));
        if (profileHeader != null && !profileHeader.isBlank()) {
            effectiveHeaders.put("X-Profile", profileHeader);
        }

        String sessionId = null;
        try {
            sessionId = mcpSessionService.getSessionId();
        } catch (Exception e) {
            log.warn("[MCP][RestTemplate] No se pudo resolver mcp-session-id: {}", e.getMessage());
        }
        if (sessionId != null && !sessionId.isBlank()) {
            effectiveHeaders.put("mcp-session-id", sessionId.trim());
        }

        log.info("[MCP][RestTemplate] Llamando MCP endpoint={} method={} authToken={} headers={} params={} ",
                endpoint,
                method,
                authToken == null ? "<empty>" : maskToken(authToken),
                effectiveHeaders,
                params == null ? Map.of() : params);

        Map<String, Object> payload = new HashMap<>();
        payload.put("jsonrpc", "2.0");
        payload.put("id", UUID.randomUUID().toString());
        payload.put("method", method);
        payload.put("params", params != null ? params : Map.of());

        HttpHeaders httpHeaders = new HttpHeaders();
        effectiveHeaders.forEach(httpHeaders::set);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, httpHeaders);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(endpoint, request, String.class);
            String body = resp.getBody();
            return parseResponse(body);
        } catch (Exception e) {
            McpResponse error = new McpResponse();
            error.setCode("MW005");
            error.setMessage(e.getMessage());
            return error;
        }
    }

    private McpResponse parseResponse(String body) {
        if (body == null || body.isBlank()) {
            McpResponse empty = new McpResponse();
            empty.setCode("MW005");
            empty.setMessage("Respuesta MCP vacía");
            return empty;
        }
        try {
            var root = objectMapper.readTree(body);
            if (root.has("error")) {
                var err = root.get("error");
                String code = err.has("code") ? err.get("code").asText() : "MW005";
                String msg = err.has("message") ? err.get("message").asText() : "Error MCP";
                McpResponse resp = new McpResponse();
                resp.setCode(code);
                resp.setMessage(msg);
                return resp;
            }
            var dataNode = root.has("result") ? root.get("result") : root;
            Map<String, Object> data = objectMapper.convertValue(dataNode, new TypeReference<Map<String, Object>>() {});
            McpResponse resp = new McpResponse();
            resp.setData(data);
            resp.setCode("OK");
            resp.setMessage("OK");
            return resp;
        } catch (Exception e) {
            McpResponse error = new McpResponse();
            error.setCode("MW005");
            error.setMessage("Error parseando respuesta MCP: " + e.getMessage());
            return error;
        }
    }

    private String resolveApiKey(Map<String, String> headers) {
        String primary = bffProperties.getMcp().getAuthToken();
        if (isPlaceholder(primary)) primary = null;
        String fallback = bffProperties.getMcp().getTestApiKey();
        if (isPlaceholder(fallback)) fallback = null;
        String fromHeader = headers == null ? null : Optional.ofNullable(headers.get("X-Api-Key")).orElse(headers.get("x-api-key"));
        if (fromHeader != null && !fromHeader.isBlank()) return fromHeader.trim();
        return Optional.ofNullable(primary).orElse(fallback);
    }

    private boolean isPlaceholder(String token) {
        if (token == null) return true;
        String t = token.trim().toUpperCase();
        return t.isEmpty() || "YOUR_API_KEY".equals(t) || "DEMO".equals(t);
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) return "<empty>";
        if (token.length() <= 6) return "***" + token.charAt(token.length() - 1);
        return token.substring(0, 3) + "..." + token.substring(token.length() - 3);
    }

    private String sanitizeTransport(String configuredTransport) {
        String transport = Optional.ofNullable(configuredTransport).orElse("http").trim();
        if (!transport.equalsIgnoreCase("http")) {
            log.warn("[MCP][RestTemplate] Transport '{}' no soportado por Odoo llm_mcp_server; forzando 'http'", transport);
            transport = "http";
        }
        return transport;
    }
}
