package com.lujanita.bff.model.dto;

public class ChatResponse {
    private String response;
    private String correlationId;
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}

