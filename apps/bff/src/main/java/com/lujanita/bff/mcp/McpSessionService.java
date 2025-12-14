package com.lujanita.bff.mcp;

import com.lujanita.bff.config.BffProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class McpSessionService {
    private final BffProperties bffProperties;
    private final AtomicReference<String> cachedSession = new AtomicReference<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public McpSessionService(BffProperties bffProperties) {
        this.bffProperties = bffProperties;
    }

    public String getSessionId() {
        String staticSession = Optional.ofNullable(bffProperties.getMcp().getStaticSessionId())
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElse(null);
        if (staticSession != null) {
            return staticSession;
        }
        String existing = cachedSession.get();
        if (existing != null) {
            return existing;
        }
        synchronized (cachedSession) {
            existing = cachedSession.get();
            if (existing != null) {
                return existing;
            }
            String sessionFromServer = fetchSessionFromServer();
            cachedSession.set(sessionFromServer);
            return sessionFromServer;
        }
    }

    public void invalidateSession() {
        cachedSession.set(null);
    }

    /**
     * Fuerza un handshake POST al endpoint de sesión y actualiza el cache con el valor obtenido.
     * Retorna el sessionId resultante (o UUID si no fue posible obtener cookie).
     */
    public String startSession() {
        synchronized (cachedSession) {
            String sessionFromServer = fetchSessionFromServer();
            cachedSession.set(sessionFromServer);
            return sessionFromServer;
        }
    }

    private String fetchSessionFromServer() {
        String baseEndpoint = Optional.ofNullable(bffProperties.getMcp().getEndpoint()).orElse("");
        String sessionEndpoint = Optional.ofNullable(bffProperties.getMcp().getSessionEndpoint())
                .filter(s -> !s.isBlank())
                .orElse(baseEndpoint.endsWith("/mcp") ? baseEndpoint + "/session" : baseEndpoint + "/mcp/session");
        if (baseEndpoint.isBlank()) {
            throw new IllegalStateException("Endpoint MCP no configurado");
        }

        HttpHeaders headers = new HttpHeaders();
        String apiKey = resolveApiKey();
        if (apiKey == null) {
            throw new IllegalStateException("API key MCP no configurada (authToken/testApiKey vacíos o placeholders)");
        }
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("X-Api-Key", apiKey);
        String transport = Optional.ofNullable(bffProperties.getMcp().getTransport()).orElse("streamable-http").trim();
        if (!transport.isBlank()) {
            headers.set("MCP-Transport", transport);
        }
        Optional.ofNullable(bffProperties.getMcp().getTestRole()).filter(s -> !s.isBlank()).ifPresent(r -> headers.set("X-Role", r));
        Optional.ofNullable(bffProperties.getMcp().getTestProfile()).filter(s -> !s.isBlank()).ifPresent(p -> headers.set("X-Profile", p));

        String[] endpointsToTry = new String[]{sessionEndpoint, baseEndpoint};
        for (String ep : endpointsToTry) {
            String sessionFromGet = tryGetCookie(ep, headers, apiKey);
            if (sessionFromGet != null) return sessionFromGet;
            String sessionFromPost = tryPostHandshake(ep, headers, apiKey);
            if (sessionFromPost != null) return sessionFromPost;
        }

        String fallbackSession = UUID.randomUUID().toString();
        log.warn("[MCP][Session] Usando session provisional: {}", fallbackSession);
        return fallbackSession;
    }

    private String extractSessionIdCookie(java.util.List<String> setCookies) {
        if (setCookies == null) return null;
        for (String c : setCookies) {
            if (c == null) continue;
            for (String part : c.split(";")) {
                String trimmed = part.trim();
                if (trimmed.startsWith("session_id=")) {
                    return trimmed.substring("session_id=".length());
                }
            }
        }
        return null;
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) return "<empty>";
        if (token.length() <= 6) return "***" + token.charAt(token.length() - 1);
        return token.substring(0, 3) + "..." + token.substring(token.length() - 3);
    }

    private String resolveApiKey() {
        String primary = bffProperties.getMcp().getAuthToken();
        if (isPlaceholder(primary)) primary = null;
        String fallback = bffProperties.getMcp().getTestApiKey();
        if (isPlaceholder(fallback)) fallback = null;
        return Optional.ofNullable(primary).orElse(fallback);
    }

    public String resolveApiKeyPublic() {
        return resolveApiKey();
    }

    private boolean isPlaceholder(String token) {
        if (token == null) return true;
        String t = token.trim().toUpperCase();
        return t.isEmpty() || "YOUR_API_KEY".equals(t) || "DEMO".equals(t);
    }

    private String tryGetCookie(String endpoint, HttpHeaders headers, String apiKey) {
        try {
            log.info("[MCP][Session] Solicitando session_id vía GET {} (apiKey={})", endpoint, maskToken(apiKey));
            ResponseEntity<String> getResp = restTemplate.exchange(endpoint, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            java.util.List<String> setCookies = getResp.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (setCookies != null) {
                log.info("[MCP][Session][GET] Set-Cookie headers: {}", setCookies);
            }
            String sessionIdCookie = extractSessionIdCookie(setCookies);
            if (sessionIdCookie != null && !sessionIdCookie.isBlank()) {
                log.info("[MCP][Session] session_id obtenido de cookie: {}", sessionIdCookie);
                return sessionIdCookie;
            }
        } catch (Exception e) {
            log.warn("[MCP][Session] GET para session_id falló: {}", e.getMessage());
        }
        return null;
    }

    private String tryPostHandshake(String endpoint, HttpHeaders headers, String apiKey) {
        try {
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "jsonrpc", "2.0",
                    "id", UUID.randomUUID().toString(),
                    "method", "handshake",
                    "params", Map.of("protocol", "mcp", "client", "lujanita-bff")
            );
            log.info("[MCP][Session] Intentando handshake en {} (apiKey={})", endpoint, maskToken(apiKey));
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(endpoint, request, String.class);
            java.util.List<String> setCookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (setCookies != null) {
                log.info("[MCP][Session][POST] Set-Cookie headers: {}", setCookies);
            }
            String sessionIdCookie = extractSessionIdCookie(setCookies);
            if (sessionIdCookie != null && !sessionIdCookie.isBlank()) {
                log.info("[MCP][Session] session_id obtenido tras POST handshake: {}", sessionIdCookie);
                return sessionIdCookie;
            }
        } catch (Exception e) {
            log.warn("[MCP][Session] POST handshake falló: {}", e.getMessage());
        }
        return null;
    }
}
