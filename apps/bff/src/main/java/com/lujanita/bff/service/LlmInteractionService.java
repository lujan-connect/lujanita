package com.lujanita.bff.service;

import com.lujanita.bff.ollama.OllamaClientService;
import com.lujanita.bff.config.BffProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Map;
import java.util.List;

@Service
@Slf4j
public class LlmInteractionService {

    @Autowired
    private OllamaClientService ollamaClientService;
    @Autowired
    private BffProperties bffProperties;
    @Autowired(required = false)
    private MeterRegistry meterRegistry;

    public String generate(String message, String role, String profile, String corrId) {
        String model = bffProperties.getOllama().getModel();
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("El modelo de Ollama no está configurado en application.yml");
        }
        long t0 = System.currentTimeMillis();
        log.info("[LLM][{}] Generate start model={} role={} profile={} ", corrId, model, role, profile);
        String resp = ollamaClientService.generate(model, message, role, profile);
        long durMs = System.currentTimeMillis() - t0;
        try {
            if (meterRegistry != null) {
                Timer.builder("llm.generate")
                    .description("Duration of LLM generate calls")
                    .tag("model", model)
                    .tag("role", String.valueOf(role))
                    .tag("profile", String.valueOf(profile))
                    .register(meterRegistry)
                    .record(durMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        } catch (Exception ignore) {}
        int len = resp == null ? 0 : resp.length();
        String preview = resp == null ? "" : (resp.length() > 500 ? resp.substring(0, 500) + "..." : resp);
        log.info("[LLM][{}] Generate done durMs={} chars={} preview=\n{}", corrId, durMs, len, preview);
        return resp;
    }

    // Plan con tooling: pedir al LLM que devuelva JSON con mcpCall válido
    public Map<String, Object> planWithTools(String message, String role, String profile, java.util.List<Map<String, Object>> tools, String corrId) {
        String model = bffProperties.getOllama().getModel();
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("El modelo de Ollama no está configurado en application.yml");
        }
        String toolsJson;
        try {
            toolsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(tools);
        } catch (Exception e) {
            toolsJson = "[]";
        }
        String planningPrompt = message + "\n\nHerramientas disponibles (JSON):\n" + toolsJson + "\n\nDevuelve estrictamente JSON con {\"mcpCall\":{\"method\":<string>,\"params\":<object>}}. No incluyas texto adicional.";
        long t0 = System.currentTimeMillis();
        log.info("[LLM][{}] Planning with tools start model={} toolsCount={} ", corrId, model, tools.size());
        String resp = ollamaClientService.generate(model, planningPrompt, role, profile);
        long durMs = System.currentTimeMillis() - t0;
        try {
            if (meterRegistry != null) {
                Timer.builder("llm.planWithTools")
                    .description("Duration of LLM planning with tooling")
                    .tag("model", model)
                    .tag("role", String.valueOf(role))
                    .tag("profile", String.valueOf(profile))
                    .register(meterRegistry)
                    .record(durMs, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        } catch (Exception ignore) {}
        log.info("[LLM][{}] Planning with tools done durMs={} chars={} ", corrId, durMs, resp == null ? 0 : resp.length());
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(resp == null ? "{}" : resp, java.util.Map.class);
            return parsed;
        } catch (Exception e) {
            return java.util.Collections.emptyMap();
        }
    }
}
