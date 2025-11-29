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

@Service
public class OllamaClientService {
    private static final Logger log = LoggerFactory.getLogger(OllamaClientService.class);

    @Autowired
    private BffProperties bffProperties;

    @Autowired
    private OllamaChatClient ollamaChatClient;

    public String generate(String model, String prompt) {
        String endpoint = bffProperties.getOllama().getEndpoint();
        // Forzar SIEMPRE el modelo configurado en YAML (tinyllama)
        String modelName = bffProperties.getOllama().getModel();
        int timeout = bffProperties.getOllama().getTimeoutMs();
        log.info("[Ollama] Usando modelo: {} (endpoint: {})", modelName, endpoint);

        // Construir prompt final: prefijar systemPrompt si está configurado
        String systemPrompt = bffProperties.getOllama().getSystemPrompt();
        String botName = bffProperties.getChatbotName();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            // Si el prompt contiene un placeholder explícito, úsalo
            if (systemPrompt.contains("{chatbotName}") && botName != null) {
                systemPrompt = systemPrompt.replace("{chatbotName}", botName);
            }
        }
        String assistantGuidelines = bffProperties.getOllama().getAssistantGuidelines();
        // Render welcome message using configuration; no defaults en Java
        String welcomeTemplate = bffProperties != null ? bffProperties.getWelcomeMessage() : "";
        String welcomeRendered = "";
        if (welcomeTemplate != null && !welcomeTemplate.isBlank()) {
            if (botName != null) {
                welcomeRendered = String.format(welcomeTemplate, botName);
            } else {
                welcomeRendered = welcomeTemplate;
            }
        }

        StringBuilder sb = new StringBuilder();
        // Construir prompt final solamente a partir de las propiedades (no hardcode en Java)
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            sb.append(systemPrompt.trim()).append("\n\n");
        }
        if (assistantGuidelines != null && !assistantGuidelines.isBlank()) {
            String guidelinesRendered = assistantGuidelines;
            if (welcomeRendered != null && !welcomeRendered.isBlank()) {
                guidelinesRendered = assistantGuidelines.replace("%s", welcomeRendered);
            }
            sb.append(guidelinesRendered).append("\n\n");
        }
        // Ajustar el prompt para dar prioridad al mensaje del usuario
        sb.append("Instrucciones del sistema: ").append(systemPrompt == null ? "" : systemPrompt.trim()).append("\n\n");
        sb.append("Contexto: ").append(assistantGuidelines == null ? "" : assistantGuidelines.trim()).append("\n\n");
        sb.append("Mensaje del usuario: ").append(prompt == null ? "" : prompt.trim()).append("\n");
        sb.append("Respuesta del asistente: "); // Preparar el espacio para la respuesta del asistente
        String finalPrompt = sb.toString();

        try {
            // Crear un mensaje de usuario con el prompt final utilizando Spring AI
            UserMessage userMessage = new UserMessage(finalPrompt);
            Prompt chatPrompt = new Prompt(List.of(userMessage));

            // Llamar al cliente de Ollama con el prompt
            ChatResponse chatResponse = ollamaChatClient.call(chatPrompt);

            // Manejar la respuesta utilizando las clases de Spring AI
            String result = chatResponse.getResult().getOutput().getContent();
            return result == null ? "" : result.trim();
        } catch (Exception e) {
            log.error("[Ollama] Error al conectar con el modelo '{}': {}", modelName, e.getMessage());
            throw new RuntimeException("Error al conectar con Ollama Spring AI (modelo: " + modelName + "): " + e.getMessage(), e);
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
