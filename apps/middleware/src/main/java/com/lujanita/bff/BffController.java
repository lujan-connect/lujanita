package com.lujanita.bff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BffController {
    @Autowired
    private BffOrchestratorService orchestrator;

    private static final Logger log = LoggerFactory.getLogger(BffController.class);
    // Feature flags (ejemplo)
    private static final boolean FEATURE_OLLAMA = true;
    private static final boolean FEATURE_MCP = true;

    /**
     * Endpoint de chat conversacional (orquesta con Ollama y MCP si corresponde)
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestHeader Map<String, String> headers, @RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        String resp;
        String correlationId = UUID.randomUUID().toString();
        try {
            if (!FEATURE_OLLAMA) {
                log.warn("[BFF][{}] LLM deshabilitado por feature flag", correlationId);
                return ResponseEntity.status(503).body("{\"code\":\"LLM001\",\"correlationId\":\""+correlationId+"\"}");
            }
            resp = orchestrator.handleChat(headers, message);
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
    public ResponseEntity<String> mcpGeneric(@RequestHeader Map<String, String> headers, @PathVariable String method, @RequestBody Map<String, Object> params) {
        String resp;
        String correlationId = UUID.randomUUID().toString();
        try {
            if (!FEATURE_MCP) {
                log.warn("[BFF][{}] MCP deshabilitado por feature flag", correlationId);
                return ResponseEntity.status(503).body("{\"code\":\"OD001\",\"correlationId\":\""+correlationId+"\"}");
            }
            resp = orchestrator.handleMcpGeneric(headers, method, params);
        } catch (Exception e) {
            log.error("[BFF][{}] Error backend MCP: {}", correlationId, e.getMessage());
            return ResponseEntity.status(502).body("{\"code\":\"MW005\",\"correlationId\":\""+correlationId+"\"}");
        }
        if (resp.contains("\"code\":\"MW001\"")) {
            log.warn("[BFF][{}] Falta apiKey", correlationId);
            return ResponseEntity.status(401).body(resp);
        }
        log.info("[BFF][{}] Respuesta MCP: {}", correlationId, resp);
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint de health para monitoreo y checks de componentes
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        // Simulación de health: status, versión, componentes
        Map<String, Object> components = Map.of(
            "ollama", Map.of("status", "ok", "model", "tinyllama"),
            "odoo", Map.of("status", "ok", "latencyMs", 50)
        );
        Map<String, Object> resp = Map.of(
            "status", "ok",
            "version", "1.0.0",
            "components", components
        );
        return ResponseEntity.ok(resp);
    }
}
