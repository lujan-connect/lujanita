package com.lujanita.bff.model.dto;

import java.util.Map;

public class McpRequest {
    private String method;
    private Map<String, Object> params;
    private Map<String, String> headers;
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
}

