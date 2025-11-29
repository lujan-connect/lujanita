package com.lujanita.bff;

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
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/api")
public class BffController {
    @Autowired
    private BffOrchestratorService orchestrator;
    @Autowired
    private OllamaClientService ollamaClientService;
    @Autowired
    private McpClientService mcpClientService;

    private static final Logger log = LoggerFactory.getLogger(BffController.class);
    // Feature flags (ejemplo)
    private static final boolean FEATURE_OLLAMA = true;
    private static final boolean FEATURE_MCP = true;

    /**
     * Endpoint de chat conversacional (orquesta con Ollama y MCP si corresponde)
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestHeader Map<String, String> headers, @RequestBody Map<String, Object> body) {
        log.info("Headers received: {}", headers);  // Debug: ver qué headers llegan
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
    public ResponseEntity<McpResponse> mcpGeneric(@RequestHeader Map<String, String> headers, @PathVariable String method, @RequestBody Map<String, Object> params) {
        String correlationId = UUID.randomUUID().toString();
        McpResponse resp;
        try {
            if (!FEATURE_MCP) {
                resp = new McpResponse();
                resp.setCode("OD001");
                resp.setCorrelationId(correlationId);
                resp.setMessage("MCP deshabilitado por feature flag");
                return ResponseEntity.status(503).body(resp);
            }
            resp = orchestrator.handleMcpGeneric(headers, method, params);
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
            // Llamada rápida de prueba a Ollama
            ollamaClientService.generate("tinyllama", "health check");
            components.put("ollama", Map.of("status", "up", "model", "tinyllama"));
        } catch (Exception e) {
            components.put("ollama", Map.of("status", "down", "error", e.getMessage()));
        }

        // Verificar salud de MCP
        try {
            // Llamada rápida de prueba a MCP
            Map<String, String> dummyHeaders = Map.of("X-Api-Key", "test", "X-Role", "user", "X-Profile", "default");
            mcpClientService.callMcp("orders.get", Map.of("orderId", "health"), dummyHeaders);
            components.put("odoo", Map.of("status", "up", "latencyMs", 50));
        } catch (Exception e) {
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

    @PostConstruct
    public void init() {
        // Lógica de inicialización, si es necesaria
        log.info("Aplicación iniciada. Verificando salud de componentes...");
        healthCheck();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void healthCheck() {
        log.info("Verificando salud de componentes al iniciar la aplicación...");

        // Verificar salud de Ollama
        try {
            orchestrator.handleChat(Map.of("X-Api-Key", "test", "X-Role", "user", "X-Profile", "default"), "health check");
            log.info("✅ Ollama: UP - Conexión exitosa");
        } catch (Exception e) {
            log.warn("❌ Ollama: DOWN - Error: {}", e.getMessage());
        }

        // Verificar salud de MCP
        try {
            orchestrator.handleMcpGeneric(Map.of("X-Api-Key", "test", "X-Role", "user", "X-Profile", "default"), "orders.get", Map.of("orderId", "health"));
            log.info("✅ MCP: UP - Conexión exitosa");
        } catch (Exception e) {
            log.warn("❌ MCP: DOWN - Error: {}", e.getMessage());
        }

        log.info("Verificación de salud completada.");
    }
}
