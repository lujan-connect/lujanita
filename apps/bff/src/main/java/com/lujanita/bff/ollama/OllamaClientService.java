package com.lujanita.bff.ollama;

import com.lujanita.bff.config.BffProperties;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.ChatResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lujanita.bff.prompt.PromptConfigService;

@Service
public class OllamaClientService {
    private static final Logger log = LoggerFactory.getLogger(OllamaClientService.class);

    @Autowired
    private BffProperties bffProperties;

    @Autowired
    private OllamaChatClient ollamaChatClient;

    @Autowired
    private PromptConfigService promptConfigService;

    public String generate(String model, String prompt, String role, String profile) {
        String endpoint = bffProperties.getOllama().getEndpoint();
        String modelName = bffProperties.getOllama().getModel();
        log.info("[Ollama] Usando modelo: {} (endpoint: {})", modelName, endpoint);

        // Obtener prompts dinámicos según rol/perfil
        String systemPrompt = promptConfigService.getSystemPrompt(role, profile);
        String assistantGuidelines = promptConfigService.getAssistantGuidelines(role, profile);
        String botName = bffProperties.getChatbotName();
        String welcomeTemplate = bffProperties != null ? bffProperties.getWelcomeMessage() : "";
        String welcomeRendered = "";
        if (welcomeTemplate != null && !welcomeTemplate.isBlank()) {
            if (botName != null) {
                welcomeRendered = String.format(welcomeTemplate, botName);
            } else {
                welcomeRendered = welcomeTemplate;
            }
        }
        StringBuilder systemBuilder = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            String sys = systemPrompt;
            if (sys.contains("{chatbotName}") && botName != null) {
                sys = sys.replace("{chatbotName}", botName);
            }
            systemBuilder.append(sys.trim()).append("\n");
        }
        if (assistantGuidelines != null && !assistantGuidelines.isBlank()) {
            String guidelinesRendered = assistantGuidelines;
            if (welcomeRendered != null && !welcomeRendered.isBlank()) {
                guidelinesRendered = assistantGuidelines.replace("%s", welcomeRendered);
            }
            systemBuilder.append(guidelinesRendered.trim()).append("\n");
        }
        String systemFull = systemBuilder.toString().trim();

        // Construcción correcta de mensajes para Spring AI
        List<org.springframework.ai.chat.messages.Message> messages = new java.util.ArrayList<>();
        if (!systemFull.isBlank()) {
            messages.add(new org.springframework.ai.chat.messages.SystemMessage(systemFull));
        }
        // El mensaje del usuario debe ser el último y el foco principal
        messages.add(new UserMessage(prompt == null ? "" : prompt.trim()));
        Prompt chatPrompt = new Prompt(messages);

        try {
            ChatResponse chatResponse = ollamaChatClient.call(chatPrompt);
            String result = chatResponse.getResult().getOutput().getContent();
            return result == null ? "" : result.trim();
        } catch (Exception e) {
            log.error("[Ollama] Error al procesar el prompt: {}", e.getMessage(), e);
            // Fallback HTTP directo si Spring AI falla
            try {
                RestTemplate rt = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode body = mapper.createObjectNode();
                body.put("model", modelName);
                body.put("prompt", prompt == null ? "" : prompt.trim());
                body.put("stream", false);
                HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);
                ResponseEntity<String> resp = rt.postForEntity(endpoint, request, String.class);
                String respBody = resp.getBody();
                if (resp.getStatusCode().is2xxSuccessful() && respBody != null) {
                    return respBody;
                } else {
                    log.error("[Ollama] Fallback HTTP falló con status {} y body: {}", resp.getStatusCodeValue(), respBody);
                    throw new RuntimeException("Fallback HTTP a Ollama falló: status=" + resp.getStatusCodeValue());
                }
            } catch (Exception ex2) {
                log.error("[Ollama] Fallback HTTP también falló: {}", ex2.getMessage());
                throw new RuntimeException("Error al conectar con Ollama (modelo: " + modelName + "): " + ex2.getMessage(), ex2);
            }
        }
    }

    // Validación automática en el startup
    @EventListener(ContextRefreshedEvent.class)
    public void validateModelOnStartup() {
        String endpoint = bffProperties.getOllama().getEndpoint();
        String modelName = bffProperties.getOllama().getModel();
        String listUrl = endpoint.replace("/api/generate", "/api/tags");
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(listUrl, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().contains(modelName)) {
                log.info("[Ollama] Modelo '{}' disponible en {}", modelName, listUrl);
            } else {
                log.error("[Ollama] El modelo '{}' NO está disponible en Ollama. Descárgalo con: ollama pull {}", modelName, modelName);
                throw new IllegalStateException("El modelo '" + modelName + "' no está disponible en Ollama. Descárgalo con: ollama pull " + modelName);
            }
        } catch (RestClientException e) {
            log.error("[Ollama] No se pudo verificar el modelo Ollama en {}: {}", listUrl, e.getMessage());
            throw new IllegalStateException("No se pudo verificar el modelo Ollama en " + listUrl + ": " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si el modelo Ollama está disponible localmente.
     * Lanza excepción si no está disponible.
     */
    public void validateModelAvailable() {
        // Si necesitas validación, implementa usando Spring AI o elimina este método si no es relevante
    }
}
