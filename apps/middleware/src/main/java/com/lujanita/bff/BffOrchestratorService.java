package com.lujanita.bff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.lujanita.bff.ollama.OllamaClientService;
import com.lujanita.bff.mcp.McpClientService;
import com.lujanita.bff.model.dto.McpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.lujanita.bff.config.BffProperties;
import java.util.UUID;

@Service
public class BffOrchestratorService {

    @Autowired
    private OllamaClientService ollamaClientService;
    @Autowired
    private McpClientService mcpClientService;
    @Autowired
    private BffProperties bffProperties;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String handleChat(Map<String, String> headers, String message) {
        // 1. Validar headers (apiKey, role, profile)
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

        // Delegar TODO el entendimiento y parsing al LLM; luego normalizamos su salida
        try {
            // Forzar modelo explícito desde configuración
            String model = bffProperties.getOllama().getModel();
            if (model == null || model.isBlank()) {
                throw new IllegalStateException("El modelo de Ollama no está configurado en application.yml");
            }

            // Llamar al servicio con modelo explícito
            String ollamaResp = ollamaClientService.generate(model, message);

            // Sanitizar respuesta contra filtrado literal de prompt
            String systemPrompt = bffProperties.getOllama().getSystemPrompt();
            String assistantGuidelines = bffProperties.getOllama().getAssistantGuidelines();
            String cleaned = ollamaResp == null ? "" : ollamaResp;
            if (systemPrompt != null && !systemPrompt.isBlank()) cleaned = cleaned.replace(systemPrompt, "");
            if (assistantGuidelines != null && !assistantGuidelines.isBlank()) cleaned = cleaned.replace(assistantGuidelines, "");
            cleaned = cleaned.trim();

            // Leer palabras clave de filtrado desde configuración
            java.util.List<String> keywords = bffProperties.getLlmFilterKeywords();
            if (keywords == null) keywords = java.util.Collections.emptyList();

            String fullyCleaned = filterLines(cleaned, keywords);

            // Intentar parsear como JSON
            try {
                Map<String, Object> parsed = objectMapper.readValue(fullyCleaned, new TypeReference<Map<String,Object>>(){});
                Map<String, Object> filteredParsed = filterJsonStrings(parsed, keywords);
                if (filteredParsed.containsKey("response")) {
                    Object respVal = filteredParsed.get("response");
                    String respText = respVal != null ? respVal.toString().trim() : "";
                    String corr = filteredParsed.containsKey("correlationId") ? String.valueOf(filteredParsed.get("correlationId")) : UUID.randomUUID().toString();
                    Map<String, String> out = Map.of("response", respText, "correlationId", corr);
                    return objectMapper.writeValueAsString(out);
                } else {
                    String respText = objectMapper.writeValueAsString(filteredParsed);
                    Map<String, String> out = Map.of("response", respText, "correlationId", UUID.randomUUID().toString());
                    return objectMapper.writeValueAsString(out);
                }
            } catch (Exception ex) {
                // No es JSON: devolver como texto plano dentro del campo response, limpiando líneas con instrucciones
                Map<String, String> out = Map.of("response", fullyCleaned, "correlationId", UUID.randomUUID().toString());
                return objectMapper.writeValueAsString(out);
            }

        } catch (Exception e) {
            return "{\"response\":\"Lo siento, no puedo ayudar con eso ahora\",\"correlationId\":\"fallback\"}";
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

    // Filtra líneas que contengan palabras clave (case-insensitive)
    private String filterLines(String text, java.util.List<String> keywords) {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\r?\n")) {
            boolean skip = false;
            String l = line.toLowerCase();
            for (String kw : keywords) {
                if (l.contains(kw.toLowerCase())) { skip = true; break; }
            }
            if (!skip && !l.trim().isEmpty()) sb.append(line).append("\n");
        }
        return sb.toString().trim();
    }

    // Limpia recursivamente todos los campos string de un JSON (Map/List)
    private Map<String, Object> filterJsonStrings(Map<String, Object> map, java.util.List<String> keywords) {
        Map<String, Object> out = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof String) {
                out.put(entry.getKey(), filterLines((String)val, keywords));
            } else if (val instanceof Map) {
                out.put(entry.getKey(), filterJsonStrings((Map<String, Object>)val, keywords));
            } else if (val instanceof java.util.List) {
                out.put(entry.getKey(), filterJsonList((java.util.List<?>)val, keywords));
            } else {
                out.put(entry.getKey(), val);
            }
        }
        return out;
    }

    private java.util.List<?> filterJsonList(java.util.List<?> list, java.util.List<String> keywords) {
        java.util.List<Object> out = new java.util.ArrayList<>();
        for (Object val : list) {
            if (val instanceof String) {
                out.add(filterLines((String)val, keywords));
            } else if (val instanceof Map) {
                out.add(filterJsonStrings((Map<String, Object>)val, keywords));
            } else if (val instanceof java.util.List) {
                out.add(filterJsonList((java.util.List<?>)val, keywords));
            } else {
                out.add(val);
            }
        }
        return out;
    }
}
