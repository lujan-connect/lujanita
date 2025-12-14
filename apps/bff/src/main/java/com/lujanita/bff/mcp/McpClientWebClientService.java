package com.lujanita.bff.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lujanita.bff.config.BffProperties;
import com.lujanita.bff.model.dto.McpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpClientWebClientService {
    private final BffProperties bffProperties;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final McpSessionService mcpSessionService;

    public Mono<McpResponse> callMcp(String method, Map<String, Object> params, Map<String, String> headers) {
        String endpoint = bffProperties.getMcp().getEndpoint();
        WebClient client = webClientBuilder.baseUrl(endpoint).build();

        // Asegurar sesión antes de construir headers (handshake si es necesario)
        String ensuredSession = null;
        try {
            ensuredSession = mcpSessionService.getSessionId();
            if (ensuredSession == null || ensuredSession.isBlank()) {
                ensuredSession = mcpSessionService.startSession();
            }
            log.info("[MCP][WebClient] Sesión asegurada mcpSession={} para method={}", ensuredSession == null ? "<none>" : maskToken(ensuredSession), method);
        } catch (Exception e) {
            log.warn("[MCP][WebClient] No se pudo asegurar sesión: {}", e.getMessage());
        }
        Map<String, String> effectiveHeaders = new HashMap<>();
        if (headers != null) {
            effectiveHeaders.putAll(headers);
        }
        // Resolver API key: primero properties/env, luego header entrante
        String authToken = resolveApiKey();
        if ((authToken == null || authToken.isBlank()) && headers != null) {
            String fromHeader = Optional.ofNullable(headers.get("X-Api-Key")).orElse(headers.get("x-api-key"));
            if (fromHeader != null && !fromHeader.isBlank()) {
                authToken = fromHeader.trim();
            }
        }
        if (authToken != null && !authToken.isBlank()) {
            String token = authToken.trim();
            effectiveHeaders.put("Authorization", "Bearer " + token);
            effectiveHeaders.put("X-Api-Key", token); // forzar X-Api-Key real aunque el cliente envíe demo
        }
        // Alinear role/profile desde headers entrantes si existen
        if (headers != null) {
            String roleHeader = Optional.ofNullable(headers.get("X-Role")).orElse(headers.get("x-role"));
            if (roleHeader != null && !roleHeader.isBlank()) {
                effectiveHeaders.put("X-Role", roleHeader);
            }
            String profileHeader = Optional.ofNullable(headers.get("X-Profile")).orElse(headers.get("x-profile"));
            if (profileHeader != null && !profileHeader.isBlank()) {
                effectiveHeaders.put("X-Profile", profileHeader);
            }
        }
        String sessionId = ensuredSession != null ? ensuredSession : resolveSessionId();
        if (sessionId != null && !sessionId.isBlank()) {
            effectiveHeaders.put("mcp-session-id", sessionId);
        }
        String transport = sanitizeTransport(bffProperties.getMcp().getTransport());
        effectiveHeaders.put("MCP-Transport", transport);

        log.info("[MCP][WebClient] Llamando MCP endpoint={} method={} authToken={} mcpSession={} headers=\n{}",
                endpoint,
                method,
                maskToken(authToken),
            sessionId == null ? "<none>" : maskToken(sessionId),
                effectiveHeaders);

        Map<String, Object> payload = new HashMap<>();
        payload.put("jsonrpc", "2.0");
        payload.put("id", UUID.randomUUID().toString());
        payload.put("method", method);
        payload.put("params", params != null ? params : Map.of());

        return client.post()
            .uri("")
            .contentType(MediaType.APPLICATION_JSON)
            .headers(httpHeaders -> effectiveHeaders.forEach(httpHeaders::set))
            .bodyValue(payload)
            .exchangeToMono(resp -> resp.bodyToMono(String.class).defaultIfEmpty(""))
            .retryWhen(
                Retry.fixedDelay(3, Duration.ofMillis(300))
                    .filter(e -> e instanceof reactor.netty.http.client.PrematureCloseException)
                    .onRetryExhaustedThrow((spec, signal) -> signal.failure())
            )
            .map(body -> {
                String truncated = body;
                if (truncated != null && truncated.length() > 500) {
                    truncated = truncated.substring(0, 500) + "...";
                }
                log.info("[MCP][WebClient] Respuesta HTTP body={}", truncated);
                return body;
            })
            .map(body -> parseResponse(body))
            .onErrorResume(e -> {
                log.warn("[MCP][WebClient] Error llamando MCP: {}", e.getMessage());
                return Mono.just(errorResponse(e));
            });
    }

    private McpResponse errorResponse(Throwable e) {
        McpResponse error = new McpResponse();
        error.setCode("MW005");
        error.setMessage("Error MCP WebClient: " + e.getMessage());
        return error;
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
            log.warn("[MCP][WebClient] Error deserializando respuesta MCP: {}", e.getMessage());
            return errorResponse(e);
        }
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) return "<empty>";
        if (token.length() <= 6) return "***" + token.charAt(token.length() - 1);
        return token.substring(0, 3) + "..." + token.substring(token.length() - 3);
    }

    private String sanitizeTransport(String configuredTransport) {
        String transport = Optional.ofNullable(configuredTransport).orElse("http").trim();
        if (!transport.equalsIgnoreCase("http")) {
            log.warn("[MCP][WebClient] Transport '{}' no soportado por Odoo llm_mcp_server; forzando 'http'", transport);
            transport = "http";
        }
        return transport;
    }

    private String resolveApiKey() {
        String primary = bffProperties.getMcp().getAuthToken();
        if (isPlaceholder(primary)) primary = null;
        String fallback = bffProperties.getMcp().getTestApiKey();
        if (isPlaceholder(fallback)) fallback = null;
        return Optional.ofNullable(primary).orElse(fallback);
    }

    private boolean isPlaceholder(String token) {
        if (token == null) return true;
        String t = token.trim().toUpperCase();
        return t.isEmpty() || "YOUR_API_KEY".equals(t) || "DEMO".equals(t);
    }

    private String resolveSessionId() {
        try {
            String sid = mcpSessionService.getSessionId();
            if (sid == null) return null;
            sid = sid.trim();
            return sid.isEmpty() ? null : sid;
        } catch (Exception e) {
            log.warn("[MCP][WebClient] No se pudo resolver mcp-session-id: {}", e.getMessage());
            return null;
        }
    }
}
