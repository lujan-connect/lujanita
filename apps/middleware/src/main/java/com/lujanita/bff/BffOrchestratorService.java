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
        // 1. Validar headers (apiKey, role, profile) - normalizar claves a minúsculas
        String apiKey = headers.getOrDefault("x-api-key", headers.get("X-Api-Key"));
        String role = headers.getOrDefault("x-role", headers.getOrDefault("X-Role", ""));
        String profile = headers.getOrDefault("x-profile", headers.getOrDefault("X-Profile", ""));
        if (apiKey == null) {
            return "{\"code\":\"MW001\",\"message\":\"Falta apiKey\"}";
        }
        if (!role.equals("user") || !profile.equals("default")) {
            return "{\"code\":\"MW002\",\"message\":\"Rol o perfil inválido\"}";
        }
        if (message == null || message.isBlank()) {
            return "{\"code\":\"MW003\",\"message\":\"Mensaje vacío\"}";
        }
        // Ya no mockea la respuesta del LLM, solo mockea MCP si es necesario
        try {
            String ollamaResp = ollamaClientService.generate("tinyllama", message);
            return ollamaResp;
        } catch (Exception e) {
            return "{\"response\":\"Hola, soy Lujanita. Ollama no está disponible, pero puedo ayudarte con consultas básicas.\",\"correlationId\":\"fallback\"}";
        }
    }

    public McpResponse handleMcpGeneric(Map<String, String> headers, String method, Map<String, Object> params) {
        String apiKey = headers.getOrDefault("x-api-key", headers.get("X-Api-Key"));
        if (apiKey == null) {
            McpResponse error = new McpResponse();
            error.setCode("MW001");
            error.setMessage("Falta apiKey");
            return error;
        }
        return mcpClientService.callMcp(method, params, headers);
    }
}
