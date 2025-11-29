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
        if (!headers.containsKey("X-Api-Key")) {
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
        String ollamaResp = ollamaClientService.generate("tinyllama", message);
        // 4. Componer respuesta final
        return ollamaResp;
    }

    public String handleOrder(Map<String, String> headers, String orderId) {
        if (!headers.containsKey("X-Api-Key")) {
            return "{\"code\":\"MW001\"}";
        }
        // Llamar a MCP para obtener la orden
        Map<String, Object> params = Map.of("orderId", orderId);
        return mcpClientService.callMcp("orders.get", params, headers);
    }

    public McpResponse handleMcpGeneric(Map<String, String> headers, String method, Map<String, Object> params) {
        if (!headers.containsKey("X-Api-Key")) {
            McpResponse error = new McpResponse();
            error.setCode("MW001");
            error.setMessage("Falta apiKey");
            return error;
        }
        return mcpClientService.callMcp(method, params, headers);
    }
}
