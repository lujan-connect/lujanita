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

    public Ollama getOllama() { return ollama; }
    public void setOllama(Ollama ollama) { this.ollama = ollama; }
    public Mcp getMcp() { return mcp; }
    public void setMcp(Mcp mcp) { this.mcp = mcp; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }

    public static class Ollama {
        private String endpoint;
        private String model;
        private int timeoutMs;
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    }
    public static class Mcp {
        private String endpoint;
        private int timeoutMs;
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public int getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }
    }
    public static class RateLimit {
        private boolean enabled;
        private int requestsPerMinute;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    }
}

