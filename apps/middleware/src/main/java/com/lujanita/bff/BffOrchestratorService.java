package com.lujanita.bff;

import com.lujanita.bff.ollama.OllamaClientService;
import com.lujanita.bff.mcp.McpClientService;
import com.lujanita.bff.model.dto.McpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class BffOrchestratorService {
    @Autowired
    private OllamaClientService ollamaClientService;
    @Autowired
    private McpClientService mcpClientService;

    public String handleChat(Map<String, String> headers, String message) {
        // 1. Validar headers (apiKey, role, profile)
        if (headers.get("X-Api-Key") == null) {
            return "{\"code\":\"MW001\"}";
        }
        if (!headers.getOrDefault("X-Role", "").equals("user") || !headers.getOrDefault("X-Profile", "").equals("default")) {
            return "{\"code\":\"MW002\"}";
        }
        if (message == null || message.isBlank()) {
            return "{\"code\":\"MW003\"}";
        }
        // 2. Orquestar llamada a MCP si corresponde (ejemplo: consulta de orden)
        // Aquí puedes parsear el mensaje y decidir si llamar a MCP
        // 3. Llamar a Ollama para obtener respuesta generativa
        try {
            String ollamaResp = ollamaClientService.generate("tinyllama", message);
            // 4. Componer respuesta final
            return ollamaResp;
        } catch (Exception e) {
            // Fallback si Ollama no está disponible
            return "{\"response\":\"Hola, soy Lujanita. Ollama no está disponible, pero puedo ayudarte con consultas básicas.\",\"correlationId\":\"fallback\"}";
        }
    }

    public McpResponse handleMcpGeneric(Map<String, String> headers, String method, Map<String, Object> params) {
        if (headers.get("X-Api-Key") == null) {
            McpResponse error = new McpResponse();
            error.setCode("MW001");
            error.setMessage("Falta apiKey");
            return error;
        }
        return mcpClientService.callMcp(method, params, headers);
    }
}
