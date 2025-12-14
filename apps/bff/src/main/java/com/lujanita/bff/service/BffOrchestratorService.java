package com.lujanita.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.lujanita.bff.service.LlmInteractionService;
import com.lujanita.bff.service.McpInteractionService;
import com.lujanita.bff.model.dto.McpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.lujanita.bff.config.BffProperties;
import java.util.UUID;

@Service
@Slf4j
public class BffOrchestratorService {

    @Autowired
    private LlmInteractionService llmInteractionService;
    @Autowired
    private McpInteractionService mcpInteractionService;
    @Autowired
    private BffProperties bffProperties;
    @Autowired
    private com.lujanita.bff.prompt.PromptConfigService promptConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String handleChat(Map<String, String> headers, String message) {
        String corrId = java.util.Optional.ofNullable(headers.get("x-correlation-id")).orElse(UUID.randomUUID().toString());
        // Log entrada
        try {
            String hdrJson = new ObjectMapper().writeValueAsString(headers);
            log.info("[BFF][{}] Chat request received headers={} messageLen={} ", corrId, hdrJson, message == null ? 0 : message.length());
        } catch (Exception ignore) {}
        // 1. Validar headers (apiKey, role, profile)
        String apiKey = headers.getOrDefault("x-api-key", headers.get("X-Api-Key"));
        String role = headers.getOrDefault("x-role", headers.getOrDefault("X-Role", ""));
        String profile = headers.getOrDefault("x-profile", headers.getOrDefault("X-Profile", ""));
        if (apiKey == null) {
            return "{\"code\":\"MW001\",\"message\":\"Falta apiKey\"}";
        }

        // Validar rol y perfil contra prompts.yml
        boolean validRole = promptConfigService.isValidRole(role);
        boolean validProfile = promptConfigService.isValidProfile(profile);
        if (!validRole && !validProfile) {
            return "{\"code\":\"MW002\",\"message\":\"Rol o perfil inválido\"}";
        }
        if (message == null || message.isBlank()) {
            return "{\"code\":\"MW003\",\"message\":\"Mensaje vacío\"}";
        }

        try {
            // Paso 1: Llamada inicial LLM (contexto usuario)
            log.info("[BFF][{}] Calling LLM for initial planning", corrId);
            String initialLlmResp = llmInteractionService.generate(message, role, profile, corrId);
            log.info("[BFF][{}] LLM initial response length={}", corrId, initialLlmResp != null ? initialLlmResp.length() : 0);

            // Paso 2: Sanitizar y parsear la primera respuesta
            String finalLlmInput = initialLlmResp;
            String systemPrompt = bffProperties.getOllama().getSystemPrompt();
            String assistantGuidelines = bffProperties.getOllama().getAssistantGuidelines();
            java.util.List<String> keywords = bffProperties.getLlmFilterKeywords();
            if (keywords == null) keywords = java.util.Collections.emptyList();

            String cleanedInitial = initialLlmResp == null ? "" : initialLlmResp;
            if (systemPrompt != null && !systemPrompt.isBlank()) cleanedInitial = cleanedInitial.replace(systemPrompt, "");
            if (assistantGuidelines != null && !assistantGuidelines.isBlank()) cleanedInitial = cleanedInitial.replace(assistantGuidelines, "");
            cleanedInitial = cleanedInitial.trim();
            String fullyCleanedInitial = filterLines(cleanedInitial, keywords);

            // Paso 1.5: Tooling-first en MCP
            java.util.List<Map<String, Object>> tools = mcpInteractionService.listTools(headers, corrId);
            // Paso 2: Pedir al LLM un mcpCall válido, condicionado por tooling
            Map<String, Object> planning = llmInteractionService.planWithTools(message, role, profile, tools, corrId);

            try {
                Map<String, Object> parsed = planning.isEmpty() ? objectMapper.readValue(fullyCleanedInitial, new TypeReference<Map<String,Object>>(){}) : planning;

                // Si el LLM ha devuelto una instrucción MCP explícita, ejecutarla
                if (parsed.containsKey("mcpCall") && parsed.get("mcpCall") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mcpCall = (Map<String, Object>) parsed.get("mcpCall");
                    Object methodObj = mcpCall.get("method");
                    Object paramsObj = mcpCall.get("params");
                    String mcpMethod = methodObj != null ? methodObj.toString() : null;
                    @SuppressWarnings("unchecked")
                    Map<String, Object> mcpParams = paramsObj instanceof Map ? (Map<String, Object>) paramsObj : java.util.Collections.emptyMap();

                    boolean valid = mcpInteractionService.validateToolCall(tools, mcpMethod, mcpParams);
                    if (valid && mcpMethod != null && !mcpMethod.isBlank()) {
                        log.info("[BFF][{}] Calling MCP method={} params={}", corrId, mcpMethod, mcpParams);
                        com.lujanita.bff.model.dto.McpResponse mcpResp = mcpInteractionService.call(mcpMethod, mcpParams, headers, corrId);
                        log.info("[BFF][{}] MCP call result code={} message={}", corrId, mcpResp != null ? mcpResp.getCode() : "null", mcpResp != null ? mcpResp.getMessage() : "null");

                        if (mcpResp == null || "MW005".equals(mcpResp.getCode())) {
                            String outMsg = "No puedo acceder en este momento a los datos de Odoo, por favor intenta más tarde o contacta a soporte";
                            Map<String, String> out = Map.of("response", outMsg, "correlationId", UUID.randomUUID().toString());
                            return objectMapper.writeValueAsString(out);
                        }

                        // Reinvocar al LLM con los datos MCP para generar la respuesta final
                        String mcpDataJson = objectMapper.writeValueAsString(mcpResp.getData());
                        String promptWithData = message + "\n\nDatos MCP (JSON):\n" + mcpDataJson;
                        log.info("[BFF][{}] Calling LLM with MCP data, prompt length={}", corrId, promptWithData.length());
                        finalLlmInput = llmInteractionService.generate(promptWithData, role, profile, corrId);
                        log.info("[BFF][{}] LLM final response length={}", corrId, finalLlmInput != null ? finalLlmInput.length() : 0);
                    }
                }
            } catch (Exception ignore) {
                // Si no es JSON, seguimos con la respuesta inicial
            }

            // Paso 3: Usar la respuesta final (segunda si hubo MCP) para devolver al usuario
            String cleanedFinal = finalLlmInput == null ? "" : finalLlmInput;
            if (systemPrompt != null && !systemPrompt.isBlank()) cleanedFinal = cleanedFinal.replace(systemPrompt, "");
            if (assistantGuidelines != null && !assistantGuidelines.isBlank()) cleanedFinal = cleanedFinal.replace(assistantGuidelines, "");
            cleanedFinal = cleanedFinal.trim();
            String fullyCleanedFinal = filterLines(cleanedFinal, keywords);

            try {
                Map<String, Object> parsed = objectMapper.readValue(fullyCleanedFinal, new TypeReference<Map<String,Object>>(){});
                Map<String, Object> filteredParsed = filterJsonStrings(parsed, keywords);
                if (filteredParsed.containsKey("response")) {
                    Object respVal = filteredParsed.get("response");
                    String respText = respVal != null ? respVal.toString().trim() : "";
                    String corr = filteredParsed.containsKey("correlationId") ? String.valueOf(filteredParsed.get("correlationId")) : UUID.randomUUID().toString();
                    Map<String, String> out = Map.of("response", respText, "correlationId", corr);
                    String outJson = objectMapper.writeValueAsString(out);
                    log.info("[BFF][{}] Chat response {} ", corrId, outJson);
                    return outJson;
                } else {
                    String respText = objectMapper.writeValueAsString(filteredParsed);
                    Map<String, String> out = Map.of("response", respText, "correlationId", UUID.randomUUID().toString());
                    String outJson = objectMapper.writeValueAsString(out);
                    log.info("[BFF][{}] Chat response {} ", corrId, outJson);
                    return outJson;
                }
            } catch (Exception ex) {
                // No es JSON: devolver como texto plano dentro del campo response, limpiando líneas con instrucciones
                Map<String, String> out = Map.of("response", fullyCleanedFinal, "correlationId", UUID.randomUUID().toString());
                String outJson = objectMapper.writeValueAsString(out);
                log.info("[BFF][{}] Chat response {} ", corrId, outJson);
                return outJson;
            }

        } catch (Exception e) {
            log.error("[BFF][{}] Chat error {} ", corrId, e.getMessage());
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
        Map<String, String> headersWithApiKey = new java.util.HashMap<>(headers);
        String sessionApiKey = resolveApiKeyPublic();
        if (sessionApiKey != null && !sessionApiKey.isBlank()) {
            headersWithApiKey.put("X-Api-Key", sessionApiKey);
            headersWithApiKey.put("Authorization", "Bearer " + sessionApiKey);
        }
        // Usar servicio de interacción MCP (modo bloqueante por compatibilidad)
        return mcpInteractionService.call(method, params, headersWithApiKey, java.util.UUID.randomUUID().toString());
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
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) val;
                out.put(entry.getKey(), filterJsonStrings(subMap, keywords));
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
                @SuppressWarnings("unchecked")
                Map<String, Object> subMap = (Map<String, Object>) val;
                out.add(filterJsonStrings(subMap, keywords));
            } else if (val instanceof java.util.List) {
                out.add(filterJsonList((java.util.List<?>)val, keywords));
            } else {
                out.add(val);
            }
        }
        return out;
    }

    // Resolución de API key (descarta placeholders)
    private String resolveApiKeyPublic() {
        String primary = bffProperties.getMcp().getAuthToken();
        if (isPlaceholder(primary)) primary = null;
        String fallback = bffProperties.getMcp().getTestApiKey();
        if (isPlaceholder(fallback)) fallback = null;
        return java.util.Optional.ofNullable(primary).orElse(fallback);
    }

    private boolean isPlaceholder(String token) {
        if (token == null) return true;
        String t = token.trim().toUpperCase();
        return t.isEmpty() || "YOUR_API_KEY".equals(t) || "DEMO".equals(t);
    }

// Setters para test
    public void setLlmInteractionService(LlmInteractionService s) { this.llmInteractionService = s; }
    public void setMcpInteractionService(McpInteractionService s) { this.mcpInteractionService = s; }
    public void setBffProperties(BffProperties b) { this.bffProperties = b; }
    public void setPromptConfigService(com.lujanita.bff.prompt.PromptConfigService p) { this.promptConfigService = p; }
    // Sesión eliminada; no se usa

    // Backward-compatible setters for existing tests
    @Deprecated
    public void setOllamaClientService(com.lujanita.bff.ollama.OllamaClientService o) {
        // Wrap provided client into a simple LlmInteractionService instance
        LlmInteractionService wrapper = new LlmInteractionService();
        try {
            java.lang.reflect.Field f1 = LlmInteractionService.class.getDeclaredField("ollamaClientService");
            f1.setAccessible(true);
            f1.set(wrapper, o);
            java.lang.reflect.Field f2 = LlmInteractionService.class.getDeclaredField("bffProperties");
            f2.setAccessible(true);
            f2.set(wrapper, this.bffProperties);
        } catch (Exception ignore) {}
        this.llmInteractionService = wrapper;
    }

    @Deprecated
    public void setMcpClientWebClientService(com.lujanita.bff.mcp.McpClientWebClientService m) {
        McpInteractionService wrapper = new McpInteractionService();
        try {
            java.lang.reflect.Field f = McpInteractionService.class.getDeclaredField("mcpClientWebClientService");
            f.setAccessible(true);
            f.set(wrapper, m);
        } catch (Exception ignore) {}
        this.mcpInteractionService = wrapper;
    }
}
