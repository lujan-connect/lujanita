package com.lujanita.bff.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PromptConfigServiceTest {
    private PromptConfigService service;

    @BeforeEach
    void setup() {
        service = new PromptConfigService();
        // Simular carga de prompts.yml en memoria
        Map<String, Object> config = Map.of(
            "default", Map.of(
                "systemPrompt", "default system",
                "assistantGuidelines", "default guidelines"
            ),
            "role", Map.of(
                "cliente", Map.of(
                    "systemPrompt", "cliente system",
                    "assistantGuidelines", "cliente guidelines"
                ),
                "vendedor", Map.of(
                    "systemPrompt", "vendedor system"
                )
            ),
            "profile", Map.of(
                "premium", Map.of(
                    "assistantGuidelines", "premium guidelines"
                )
            )
        );
        ReflectionTestUtils.setField(service, "config", config);
    }

    @Test
    void testGetSystemPrompt_roleMatch() {
        String prompt = service.getSystemPrompt("cliente", null);
        assertEquals("cliente system", prompt);
    }

    @Test
    void testGetSystemPrompt_profileFallback() {
        String prompt = service.getSystemPrompt(null, "premium");
        assertEquals("default system", prompt); // no systemPrompt in profile, fallback to default
    }

    @Test
    void testGetAssistantGuidelines_roleMatch() {
        String prompt = service.getAssistantGuidelines("cliente", null);
        assertEquals("cliente guidelines", prompt);
    }

    @Test
    void testGetAssistantGuidelines_profileMatch() {
        String prompt = service.getAssistantGuidelines(null, "premium");
        assertEquals("premium guidelines", prompt);
    }

    @Test
    void testIsValidRole() {
        assertTrue(service.isValidRole("cliente"));
        assertFalse(service.isValidRole("nope"));
    }

    @Test
    void testIsValidProfile() {
        assertTrue(service.isValidProfile("premium"));
        assertFalse(service.isValidProfile("nope"));
    }
}

