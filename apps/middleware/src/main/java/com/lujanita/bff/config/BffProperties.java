package com.lujanita.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bff")
public class BffProperties {
    private Ollama ollama = new Ollama();
    private Mcp mcp = new Mcp();
    private RateLimit rateLimit = new RateLimit();
    private Cors cors = new Cors();

    public Ollama getOllama() { return ollama; }
    public void setOllama(Ollama ollama) { this.ollama = ollama; }
    public Mcp getMcp() { return mcp; }
    public void setMcp(Mcp mcp) { this.mcp = mcp; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    public static class Ollama {
        private String endpoint;
        private String model;
        private int timeoutMs;
        private boolean enabled = true;
        // Prompt de sistema configurable que se prefija a cada petición al LLM
        private String systemPrompt = "Eres Lujanita, un asistente conversacional para Odoo de una empresa de logística en Argentina. Responde en castellano y explica las funcionalidades de Odoo en lenguaje natural, adaptando el tono a un usuario profesional pero accesible.";
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getSystemPrompt() { return systemPrompt; }
        public void setSystemPrompt(String systemPrompt) { this.systemPrompt = systemPrompt; }
    }
    public static class Mcp {
        private String endpoint;
        private int timeoutMs;
        private boolean enabled = true;
        private boolean mockEnabled = false;
        private String testApiKey = "test";
        private String testRole = "user";
        private String testProfile = "default";
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public int getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isMockEnabled() { return mockEnabled; }
        public void setMockEnabled(boolean mockEnabled) { this.mockEnabled = mockEnabled; }
        public String getTestApiKey() { return testApiKey; }
        public void setTestApiKey(String testApiKey) { this.testApiKey = testApiKey; }
        public String getTestRole() { return testRole; }
        public void setTestRole(String testRole) { this.testRole = testRole; }
        public String getTestProfile() { return testProfile; }
        public void setTestProfile(String testProfile) { this.testProfile = testProfile; }
    }
    public static class RateLimit {
        private boolean enabled;
        private int requestsPerMinute;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    }
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private boolean allowCredentials;
        private int maxAge;
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
        public String getAllowedMethods() { return allowedMethods; }
        public void setAllowedMethods(String allowedMethods) { this.allowedMethods = allowedMethods; }
        public String getAllowedHeaders() { return allowedHeaders; }
        public void setAllowedHeaders(String allowedHeaders) { this.allowedHeaders = allowedHeaders; }
        public boolean isAllowCredentials() { return allowCredentials; }
        public void setAllowCredentials(boolean allowCredentials) { this.allowCredentials = allowCredentials; }
        public int getMaxAge() { return maxAge; }
        public void setMaxAge(int maxAge) { this.maxAge = maxAge; }
    }
}
