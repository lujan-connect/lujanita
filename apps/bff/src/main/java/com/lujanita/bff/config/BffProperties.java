package com.lujanita.bff.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
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
        private String systemPrompt;
        // Directrices para el asistente (se lee desde application.yml)
        private String assistantGuidelines;
        // Información corporativa que se puede inyectar en cada prompt (texto libre extraído del sitio)
        private String corporateName = "";
        private String corporateWebsite = "";
        private String corporateInfo = "";
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
        public String getAssistantGuidelines() { return assistantGuidelines; }
        public void setAssistantGuidelines(String assistantGuidelines) { this.assistantGuidelines = assistantGuidelines; }
        public String getCorporateName() { return corporateName; }
        public void setCorporateName(String corporateName) { this.corporateName = corporateName; }
        public String getCorporateWebsite() { return corporateWebsite; }
        public void setCorporateWebsite(String corporateWebsite) { this.corporateWebsite = corporateWebsite; }
        public String getCorporateInfo() { return corporateInfo; }
        public void setCorporateInfo(String corporateInfo) { this.corporateInfo = corporateInfo; }
    }
    public static class Mcp {
        private String endpoint;
        private int timeoutMs;
        private boolean enabled = true;
        private boolean mockEnabled = false;
        private boolean experimentalUseRmcpClient = false;
        private String testApiKey = "test";
        private String testRole = "user";
        private String testProfile = "default";
        private String authToken;
        private String transport;
        private String sessionEndpoint;
        private String staticSessionId;
        private boolean insecureSkipTlsVerify = false;
        // Connection pool settings
        private int maxConnections = 50;
        private int maxIdleTimeSeconds = 20;
        private int maxLifeTimeSeconds = 60;
        private int pendingAcquireTimeoutSeconds = 45;
        private int evictInBackgroundSeconds = 120;
        
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public int getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public boolean isMockEnabled() { return mockEnabled; }
        public void setMockEnabled(boolean mockEnabled) { this.mockEnabled = mockEnabled; }
        public boolean isExperimentalUseRmcpClient() { return experimentalUseRmcpClient; }
        public void setExperimentalUseRmcpClient(boolean experimentalUseRmcpClient) { this.experimentalUseRmcpClient = experimentalUseRmcpClient; }
        public String getTestApiKey() { return testApiKey; }
        public void setTestApiKey(String testApiKey) { this.testApiKey = testApiKey; }
        public String getTestRole() { return testRole; }
        public void setTestRole(String testRole) { this.testRole = testRole; }
        public String getTestProfile() { return testProfile; }
        public void setTestProfile(String testProfile) { this.testProfile = testProfile; }
        public String getAuthToken() { return authToken; }
        public void setAuthToken(String authToken) { this.authToken = authToken; }
        public String getTransport() { return transport; }
        public void setTransport(String transport) { this.transport = transport; }
        public String getSessionEndpoint() { return sessionEndpoint; }
        public void setSessionEndpoint(String sessionEndpoint) { this.sessionEndpoint = sessionEndpoint; }
        public String getStaticSessionId() { return staticSessionId; }
        public void setStaticSessionId(String staticSessionId) { this.staticSessionId = staticSessionId; }
        public boolean isInsecureSkipTlsVerify() { return insecureSkipTlsVerify; }
        public void setInsecureSkipTlsVerify(boolean insecureSkipTlsVerify) { this.insecureSkipTlsVerify = insecureSkipTlsVerify; }
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }
        public int getMaxIdleTimeSeconds() { return maxIdleTimeSeconds; }
        public void setMaxIdleTimeSeconds(int maxIdleTimeSeconds) { this.maxIdleTimeSeconds = maxIdleTimeSeconds; }
        public int getMaxLifeTimeSeconds() { return maxLifeTimeSeconds; }
        public void setMaxLifeTimeSeconds(int maxLifeTimeSeconds) { this.maxLifeTimeSeconds = maxLifeTimeSeconds; }
        public int getPendingAcquireTimeoutSeconds() { return pendingAcquireTimeoutSeconds; }
        public void setPendingAcquireTimeoutSeconds(int pendingAcquireTimeoutSeconds) { this.pendingAcquireTimeoutSeconds = pendingAcquireTimeoutSeconds; }
        public int getEvictInBackgroundSeconds() { return evictInBackgroundSeconds; }
        public void setEvictInBackgroundSeconds(int evictInBackgroundSeconds) { this.evictInBackgroundSeconds = evictInBackgroundSeconds; }
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
    // Propiedades generales del chatbot (configurables desde application.yml)
    private String chatbotName;
    private String welcomeMessage;
    private java.util.List<String> llmFilterKeywords;

    public String getChatbotName() { return chatbotName; }
    public void setChatbotName(String chatbotName) { this.chatbotName = chatbotName; }
    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
    public java.util.List<String> getLlmFilterKeywords() { return llmFilterKeywords; }
    public void setLlmFilterKeywords(java.util.List<String> llmFilterKeywords) { this.llmFilterKeywords = llmFilterKeywords; }
}
