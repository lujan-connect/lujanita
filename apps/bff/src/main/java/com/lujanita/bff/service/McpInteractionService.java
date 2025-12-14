package com.lujanita.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lujanita.bff.mcp.McpClientWebClientService;
import com.lujanita.bff.config.BffProperties;
import com.lujanita.bff.model.dto.McpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class McpInteractionService {

    @Autowired
    private McpClientWebClientService mcpClientWebClientService;
    @Autowired
    private BffProperties bffProperties;
    @Autowired(required = false)
    private MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public McpResponse call(String method, Map<String, Object> params, Map<String, String> incomingHeaders, String corrId) {
        long t0 = System.currentTimeMillis();
        Map<String, String> effectiveHeaders = new java.util.HashMap<>();
        if (incomingHeaders != null) effectiveHeaders.putAll(incomingHeaders);

        String apiKey = resolveApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            effectiveHeaders.put("X-Api-Key", apiKey);
            effectiveHeaders.put("Authorization", "Bearer " + apiKey);
        }
        String sessionId = resolveSessionId();
        if (sessionId != null && !sessionId.isBlank()) {
            effectiveHeaders.put("mcp-session-id", sessionId);
        }

        try {
            String paramsJson = objectMapper.writeValueAsString(params);
            String headersJson = objectMapper.writeValueAsString(effectiveHeaders);
            log.info("[MCP][{}] Calling method={} params={} headers={} ", corrId, method, paramsJson, headersJson);
        } catch (Exception ignore) {}

        McpResponse resp;
        try {
            resp = mcpClientWebClientService.callMcp(method, params, effectiveHeaders).block();
        } catch (Exception ex) {
            log.warn("[MCP][{}] Error calling MCP: {} ", corrId, ex.getMessage());
            return null;
        }
        try {
            long durMs = System.currentTimeMillis() - t0;
            try {
                if (meterRegistry != null) {
                    Timer.builder("mcp.call")
                        .description("Duration of MCP calls")
                        .tag("method", String.valueOf(method))
                        .tag("code", resp == null ? "null" : String.valueOf(resp.getCode()))
                        .register(meterRegistry)
                        .record(durMs, java.util.concurrent.TimeUnit.MILLISECONDS);
                }
            } catch (Exception ignore) {}
            String dataJson = resp != null && resp.getData() != null ? objectMapper.writeValueAsString(resp.getData()) : "<null>";
            int dataLen = dataJson == null ? 0 : dataJson.length();
            String preview = dataJson == null ? "" : (dataLen > 500 ? dataJson.substring(0, 500) + "..." : dataJson);
            log.info("[MCP][{}] Response durMs={} code={} message={} dataLen={} preview=\n{} ", corrId,
                durMs,
                resp == null ? "<null>" : resp.getCode(),
                resp == null ? "<null>" : resp.getMessage(),
                dataLen,
                preview);
        } catch (Exception ignore) {}
        return resp;
    }

    // Tooling-first: obtener catálogo de herramientas MCP
    public java.util.List<Map<String, Object>> listTools(Map<String, String> incomingHeaders, String corrId) {
        long t0 = System.currentTimeMillis();
        McpResponse resp = call("tools.list", java.util.Collections.emptyMap(), incomingHeaders, corrId);
        try {
            if (meterRegistry != null) {
                Timer.builder("mcp.tools.list")
                    .description("Duration of MCP tooling list calls")
                    .register(meterRegistry)
                    .record(System.currentTimeMillis() - t0, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        } catch (Exception ignore) {}
        if (resp == null || resp.getData() == null) return java.util.Collections.emptyList();
        Object toolsObj = resp.getData().get("tools");
        if (toolsObj instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> tools = (java.util.List<Map<String, Object>>) toolsObj;
            return tools;
        }
        return java.util.Collections.emptyList();
    }

    // Validación básica contra tooling (nombre y params requeridos)
    public boolean validateToolCall(java.util.List<Map<String, Object>> tools, String method, Map<String, Object> params) {
        if (method == null || method.isBlank()) return false;
        Map<String, Object> found = null;
        for (Map<String, Object> t : tools) {
            Object name = t.get("name");
            if (name != null && method.equals(String.valueOf(name))) { found = t; break; }
        }
        if (found == null) return false;
        Object schemaObj = found.get("parameters");
        if (!(schemaObj instanceof Map)) return true; // sin esquema, asumimos válido
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) schemaObj;
        @SuppressWarnings("unchecked")
        Map<String, Object> props = (Map<String, Object>) schema.get("properties");
        @SuppressWarnings("unchecked")
        java.util.List<String> required = (java.util.List<String>) schema.get("required");
        if (required != null) {
            for (String r : required) {
                if (params == null || !params.containsKey(r)) return false;
            }
        }
        if (props != null && params != null) {
            for (Map.Entry<String, Object> e : params.entrySet()) {
                if (!props.containsKey(e.getKey())) return false;
            }
        }
        return true;
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
        String sidProp = bffProperties.getMcp().getStaticSessionId();
        if (sidProp == null) return null;
        sidProp = sidProp.trim();
        return sidProp.isEmpty() ? null : sidProp;
    }
}
