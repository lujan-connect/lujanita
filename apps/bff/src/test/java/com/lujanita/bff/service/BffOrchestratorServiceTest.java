package com.lujanita.bff.service;

import com.lujanita.bff.mcp.McpClientWebClientService;
import com.lujanita.bff.ollama.OllamaClientService;
import com.lujanita.bff.config.BffProperties;
import com.lujanita.bff.model.dto.McpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import reactor.core.publisher.Mono;

class BffOrchestratorServiceTest {
    private BffOrchestratorService service;
    private OllamaClientService ollamaClientService;
    private McpClientWebClientService mcpClientWebClientService;
    private BffProperties bffProperties;

    @BeforeEach
    void setup() {
        ollamaClientService = Mockito.mock(OllamaClientService.class);
        mcpClientWebClientService = Mockito.mock(McpClientWebClientService.class);
        bffProperties = Mockito.mock(BffProperties.class);
        service = new BffOrchestratorService();
        // Inyección manual usando setters
        service.setOllamaClientService(ollamaClientService);
        service.setMcpClientWebClientService(mcpClientWebClientService);
        service.setBffProperties(bffProperties);
    }

    @Test
    void handleMcpGeneric_returnsErrorIfNoApiKey() {
        Map<String, String> headers = Map.of();
        McpResponse resp = service.handleMcpGeneric(headers, "test.method", Map.of());
        assertEquals("MW001", resp.getCode());
    }

    @Test
    void handleMcpGeneric_callsWebClientService() {
        Map<String, String> headers = Map.of("x-api-key", "test");
        McpResponse expected = new McpResponse();
        expected.setCode("OK");
        Mockito.when(mcpClientWebClientService.callMcp(any(), any(), any())).thenReturn(Mono.just(expected));
        McpResponse resp = service.handleMcpGeneric(headers, "test.method", Map.of());
        assertEquals("OK", resp.getCode());
    }

    @Test
    void handleMcpGeneric_returnsErrorIfInvalidRoleOrProfile() {
        Map<String, String> headers = Map.of("x-api-key", "test", "x-role", "admin", "x-profile", "default");
        String result = service.handleChat(headers, "mensaje");
        assertTrue(result.contains("MW002"));
    }

    @Test
    void handleMcpGeneric_returnsErrorIfEmptyMessage() {
        Map<String, String> headers = Map.of("x-api-key", "test", "x-role", "user", "x-profile", "default");
        String result = service.handleChat(headers, "");
        assertTrue(result.contains("MW003"));
    }

    @Test
    void handleChat_returnsErrorIfOllamaThrows() {
        Map<String, String> headers = Map.of("x-api-key", "test", "x-role", "user", "x-profile", "default");
        Mockito.when(bffProperties.getOllama()).thenReturn(Mockito.mock(BffProperties.Ollama.class));
        Mockito.when(bffProperties.getOllama().getModel()).thenReturn("mistral");
        Mockito.when(ollamaClientService.generate(any(), any())).thenThrow(new RuntimeException("Fallo LLM"));
        String result = service.handleChat(headers, "hola");
        assertTrue(result.contains("Lo siento"));
    }

    @Test
    void handleChat_returnsJsonIfOllamaReturnsJson() {
        Map<String, String> headers = Map.of("x-api-key", "test", "x-role", "user", "x-profile", "default");
        Mockito.when(bffProperties.getOllama()).thenReturn(Mockito.mock(BffProperties.Ollama.class));
        Mockito.when(bffProperties.getOllama().getModel()).thenReturn("mistral");
        Mockito.when(bffProperties.getLlmFilterKeywords()).thenReturn(java.util.List.of());
        String json = "{\"response\":\"ok\",\"correlationId\":\"id\"}";
        Mockito.when(ollamaClientService.generate(any(), any())).thenReturn(json);
        String result = service.handleChat(headers, "hola");
        assertTrue(result.contains("ok"));
        assertTrue(result.contains("correlationId"));
    }

    @Test
    void handleChat_returnsTextIfOllamaReturnsNonJson() {
        Map<String, String> headers = Map.of("x-api-key", "test", "x-role", "user", "x-profile", "default");
        Mockito.when(bffProperties.getOllama()).thenReturn(Mockito.mock(BffProperties.Ollama.class));
        Mockito.when(bffProperties.getOllama().getModel()).thenReturn("mistral");
        Mockito.when(bffProperties.getLlmFilterKeywords()).thenReturn(java.util.List.of());
        Mockito.when(ollamaClientService.generate(any(), any())).thenReturn("Solo texto plano");
        String result = service.handleChat(headers, "hola");
        assertTrue(result.contains("Solo texto plano"));
        assertTrue(result.contains("correlationId"));
    }

    @Test
    void handleChat_filtersKeywordsFromResponse() {
        Map<String, String> headers = Map.of("x-api-key", "test", "x-role", "user", "x-profile", "default");
        Mockito.when(bffProperties.getOllama()).thenReturn(Mockito.mock(BffProperties.Ollama.class));
        Mockito.when(bffProperties.getOllama().getModel()).thenReturn("mistral");
        Mockito.when(bffProperties.getLlmFilterKeywords()).thenReturn(java.util.List.of("oculto"));
        Mockito.when(ollamaClientService.generate(any(), any())).thenReturn("{\"response\":\"ok\noculto\",\"correlationId\":\"id\"}");
        String result = service.handleChat(headers, "hola");
        assertTrue(result.contains("ok"));
        assertFalse(result.contains("oculto"));
    }
}
