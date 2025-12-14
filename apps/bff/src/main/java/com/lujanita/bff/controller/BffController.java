package com.lujanita.bff.controller;

import com.lujanita.bff.service.BffOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;
import com.lujanita.bff.model.dto.McpResponse;
import com.lujanita.bff.ollama.OllamaClientService;
import com.lujanita.bff.mcp.McpClientService;
import com.lujanita.bff.mcp.McpSessionService;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import com.lujanita.bff.config.BffProperties;
import org.springframework.core.annotation.Order;

@RestController
@RequestMapping("/api")
public class BffController {
    @Autowired
    private BffOrchestratorService orchestrator;
    @Autowired
    private OllamaClientService ollamaClientService;
    @Autowired
    private McpClientService mcpClientService;
    @Autowired
    private McpSessionService mcpSessionService;
    @Autowired
    private BffProperties bffProperties;

    private static final Logger log = LoggerFactory.getLogger(BffController.class);

    /**
     * Endpoint de chat conversacional (orquesta con Ollama y MCP si corresponde)
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestHeader Map<String, String> headers, @RequestBody Map<String, Object> body) {
        log.info("Headers received: {}", headers);  // Debug: ver qué headers llegan
        // Normalizar headers a minúsculas para evitar problemas de case
        Map<String, String> normHeaders = new HashMap<>();
        headers.forEach((k, v) -> normHeaders.put(k.toLowerCase(), v));
        String message = (String) body.get("message");
        String resp;
        String correlationId = UUID.randomUUID().toString();
        try {
            if (!bffProperties.getOllama().isEnabled()) {
                log.warn("[BFF][{}] LLM deshabilitado por feature flag", correlationId);
                return ResponseEntity.status(503).body("{\"code\":\"LLM001\",\"correlationId\":\""+correlationId+"\"}");
            }
            resp = orchestrator.handleChat(normHeaders, message);
        } catch (Exception e) {
            log.error("[BFF][{}] Error backend: {}", correlationId, e.getMessage());
            return ResponseEntity.status(502).body("{\"code\":\"MW005\",\"correlationId\":\""+correlationId+"\"}");
        }
        if (resp.contains("\"code\":\"MW001\"")) {
            log.warn("[BFF][{}] Falta apiKey", correlationId);
            return ResponseEntity.status(401).body(resp);
        }
        if (resp.contains("\"code\":\"MW002\"")) {
            log.warn("[BFF][{}] Rol/profile inválido", correlationId);
            return ResponseEntity.status(403).body(resp);
        }
        if (resp.contains("\"code\":\"MW003\"")) {
            log.warn("[BFF][{}] Payload inválido", correlationId);
            return ResponseEntity.status(400).body(resp);
        }
        // Logging de correlationId si existe
        if (resp.contains("correlationId")) {
            log.info("[BFF][{}] Respuesta chat: {}", correlationId, resp);
        }
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint genérico para MCP: ejecuta cualquier método soportado por el tooling del MCP server de Odoo
     * Ejemplo: POST /api/mcp/orders.get, /api/mcp/customers.search, etc.
     */
    @PostMapping("/mcp/{method}")
    public ResponseEntity<McpResponse> mcpGeneric(@RequestHeader Map<String, String> headers, @PathVariable String method, @RequestBody Map<String, Object> params) {
        // Normalizar headers a minúsculas
        Map<String, String> normHeaders = new HashMap<>();
        headers.forEach((k, v) -> normHeaders.put(k.toLowerCase(), v));
        String correlationId = UUID.randomUUID().toString();
        McpResponse resp;
        try {
            if (!bffProperties.getMcp().isEnabled()) {
                resp = new McpResponse();
                resp.setCode("OD001");
                resp.setCorrelationId(correlationId);
                resp.setMessage("MCP deshabilitado por feature flag");
                return ResponseEntity.status(503).body(resp);
            }
            resp = orchestrator.handleMcpGeneric(normHeaders, method, params);
        } catch (Exception e) {
            resp = new McpResponse();
            resp.setCode("MW005");
            resp.setCorrelationId(correlationId);
            resp.setMessage("Error backend MCP: " + e.getMessage());
            return ResponseEntity.status(502).body(resp);
        }
        if ("MW001".equals(resp.getCode())) {
            log.warn("[BFF][{}] Falta apiKey", correlationId);
            resp.setCorrelationId(correlationId);
            return ResponseEntity.status(401).body(resp);
        }
        log.info("[BFF][{}] Respuesta MCP: {}", correlationId, resp);
        resp.setCorrelationId(correlationId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint de health para monitoreo y checks de componentes
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> components = new HashMap<>();

        // Verificar salud de Ollama
        try {
            ollamaClientService.validateModelAvailable();
            components.put("ollama", Map.of("status", "up", "model", bffProperties.getOllama().getModel()));
        } catch (Exception e) {
            components.put("ollama", Map.of("status", "down", "error", e.getMessage()));
        }

        // Verificar salud de MCP
        String endpoint = bffProperties.getMcp().getEndpoint();
        String apiKey = bffProperties.getMcp().getTestApiKey();
        String role = bffProperties.getMcp().getTestRole();
        String profile = bffProperties.getMcp().getTestProfile();
        Map<String, String> dummyHeaders = new HashMap<>();
        dummyHeaders.put("X-Api-Key", apiKey);
        dummyHeaders.put("X-Role", role);
        dummyHeaders.put("X-Profile", profile);
        String sessionIdForLog = null;
        try {
            String sessionId = mcpSessionService.getSessionId();
            sessionIdForLog = sessionId;
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                dummyHeaders.put("mcp-session-id", sessionId.trim());
            }
        } catch (Exception ignore) {}
        log.info("[Health] MCP headers preparados: endpoint={} apiKey={} role={} profile={} mcpSession={}",
            endpoint,
            apiKey == null ? "<empty>" : (apiKey.length() <= 6 ? "***" : apiKey.substring(0,3)+"..."+apiKey.substring(apiKey.length()-3)),
            role,
            profile,
            sessionIdForLog == null ? "<none>" : (sessionIdForLog.length() <= 6 ? "***" : sessionIdForLog.substring(0,3)+"..."+sessionIdForLog.substring(sessionIdForLog.length()-3))
        );
        try {
            mcpClientService.callMcp("orders.get", Map.of("orderId", "health"), dummyHeaders);
            components.put("odoo", Map.of("status", "up", "latencyMs", 50));
        } catch (Exception e) {
            log.warn("❌ odoo: DOWN - Error: {} | endpoint={} | apiKey={} | role={} | profile={}", e.getMessage(), endpoint, apiKey, role, profile);
            components.put("odoo", Map.of("status", "down", "error", e.getMessage()));
        }

        // Determinar status general
        boolean allUp = true;
        for (Object comp : components.values()) {
            if (!"up".equals(((Map<?, ?>) comp).get("status"))) {
                allUp = false;
                break;
            }
        }
        String overallStatus = allUp ? "up" : "down";

        Map<String, Object> resp = Map.of(
            "status", overallStatus,
            "version", "1.0.0",
            "components", components
        );
        return ResponseEntity.ok(resp);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Order(1) // Ensure this runs only once and in the correct order
    public void healthCheck() {
        log.info("Verificando salud de componentes al iniciar la aplicación...");

        Map<String, Object> components = new HashMap<>();

        // Verificar salud de Ollama
        try {
            ollamaClientService.validateModelAvailable();
            components.put("ollama", Map.of("status", "up", "model", bffProperties.getOllama().getModel()));
        } catch (Exception e) {
            components.put("ollama", Map.of("status", "down", "error", e.getMessage()));
        }

        // Verificar salud de MCP
        String endpoint = bffProperties.getMcp().getEndpoint();
        String apiKey = bffProperties.getMcp().getTestApiKey();
        String role = bffProperties.getMcp().getTestRole();
        String profile = bffProperties.getMcp().getTestProfile();
        Map<String, String> dummyHeaders = new HashMap<>();
        dummyHeaders.put("X-Api-Key", apiKey);
        dummyHeaders.put("X-Role", role);
        dummyHeaders.put("X-Profile", profile);
        String sessionIdForLog2 = null;
        try {
            String sessionId = mcpSessionService.getSessionId();
            sessionIdForLog2 = sessionId;
            if (sessionId != null && !sessionId.trim().isEmpty()) {
                dummyHeaders.put("mcp-session-id", sessionId.trim());
            }
        } catch (Exception ignore) {}
        log.info("[Startup Health] MCP headers: endpoint={} apiKey={} role={} profile={} mcpSession={}",
            endpoint,
            apiKey == null ? "<empty>" : (apiKey.length() <= 6 ? "***" : apiKey.substring(0,3)+"..."+apiKey.substring(apiKey.length()-3)),
            role,
            profile,
            sessionIdForLog2 == null ? "<none>" : (sessionIdForLog2.length() <= 6 ? "***" : sessionIdForLog2.substring(0,3)+"..."+sessionIdForLog2.substring(sessionIdForLog2.length()-3))
        );
        try {
            mcpClientService.callMcp("orders.get", Map.of("orderId", "health"), dummyHeaders);
            components.put("odoo", Map.of("status", "up", "latencyMs", 50));
        } catch (Exception e) {
            log.warn("❌ odoo: DOWN - Error: {} | endpoint={} | apiKey={} | role={} | profile={}", e.getMessage(), endpoint, apiKey, role, profile);
            components.put("odoo", Map.of("status", "down", "error", e.getMessage()));
        }

        components.forEach((key, value) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> component = (Map<String, Object>) value;
            String status = (String) component.get("status");
            if ("up".equals(status)) {
                log.info("✅ {}: UP", key);
            } else {
                log.warn("❌ {}: DOWN - Error: {}", key, component.get("error"));
            }
        });

        log.info("Verificación de salud completada.");
    }
}
