package com.lujanita.bff.mcp;

import com.lujanita.bff.config.BffProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.io.IOException;

@Component
public class MockMcpAutoStarter {
    private static final Logger log = LoggerFactory.getLogger(MockMcpAutoStarter.class);

    @Autowired
    private BffProperties bffProperties;

    private HttpServer server;

    @PostConstruct
    public void startIfEnabled() {
        try {
            if (bffProperties != null && bffProperties.getMcp() != null && bffProperties.getMcp().isMockEnabled()) {
                String endpoint = bffProperties.getMcp().getEndpoint();
                if (endpoint == null || endpoint.isBlank()) endpoint = "http://localhost:8069/mcp";
                URI uri = URI.create(endpoint);
                int port = uri.getPort() == -1 ? 80 : uri.getPort();
                String path = uri.getPath();
                if (path == null || path.isBlank()) path = "/mcp";

                server = HttpServer.create(new InetSocketAddress(port), 0);
                server.createContext(path, new McpHandler());
                server.setExecutor(Executors.newCachedThreadPool());
                server.start();
                log.info("Mock MCP server started at http://localhost:{}{} (mockEnabled=true)", port, path);
            }
        } catch (Exception e) {
            log.error("Failed to start mock MCP server: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Mock MCP server stopped");
        }
    }

    static class McpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{}";
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            // Handshake: devolver header mcp-session-id simulando el servidor real
            if (body.contains("\"method\":\"handshake\"")) {
                exchange.getResponseHeaders().add("mcp-session-id", "mock-session-id");
            }
            // Simula el contrato MCP real del llm_mcp_server de Odoo (métodos comunes)
            if (body.contains("\"method\":\"orders.get\"")) {
                if (body.contains("includeLines") && body.contains("true")) {
                    response = "{" +
                        "\"orderId\":\"SO001\"," +
                        "\"status\":\"confirmed\"," +
                        "\"customerName\":\"Juan Perez\"," +
                        "\"totalAmount\":123.45," +
                        "\"createdAt\":\"2025-11-29T10:00:00Z\"," +
                        "\"lines\":[{" +
                            "\"productId\":\"P001\"," +
                            "\"productName\":\"Producto 1\"," +
                            "\"quantity\":2," +
                            "\"price\":50.0" +
                        "}]" +
                    "}";
                } else {
                    response = "{" +
                        "\"orderId\":\"SO001\"," +
                        "\"status\":\"confirmed\"," +
                        "\"customerName\":\"Juan Perez\"," +
                        "\"totalAmount\":123.45," +
                        "\"createdAt\":\"2025-11-29T10:00:00Z\"" +
                    "}";
                }
            } else if (body.contains("\"method\":\"orders.list\"")) {
                response = "{" +
                    "\"orders\":[{" +
                        "\"orderId\":\"SO001\"," +
                        "\"status\":\"confirmed\"," +
                        "\"customerName\":\"Juan Perez\"," +
                        "\"totalAmount\":123.45," +
                        "\"createdAt\":\"2025-11-29T10:00:00Z\"}]," +
                    "\"totalCount\":1," +
                    "\"limit\":20," +
                    "\"offset\":0" +
                "}";
            } else if (body.contains("\"method\":\"customers.get\"")) {
                response = "{" +
                    "\"customerId\":\"C001\"," +
                    "\"customerName\":\"Juan Perez\"," +
                    "\"email\":\"juan@demo.com\"," +
                    "\"phone\":\"123456789\"" +
                "}";
            } else if (body.contains("\"method\":\"customers.search\"")) {
                response = "{" +
                    "\"customers\":[{" +
                        "\"customerId\":\"C001\"," +
                        "\"customerName\":\"Juan Perez\"}]," +
                    "\"totalCount\":1," +
                    "\"limit\":20," +
                    "\"offset\":0" +
                "}";
            } else if (body.contains("\"method\":\"products.search\"")) {
                response = "{" +
                    "\"products\":[{" +
                        "\"productId\":\"P001\"," +
                        "\"productName\":\"Producto 1\"," +
                        "\"price\":50.0}]," +
                    "\"totalCount\":1," +
                    "\"limit\":20," +
                    "\"offset\":0" +
                "}";
            } else {
                response = "{\"ok\":true,\"message\":\"Método MCP simulado\"}";
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, respBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(respBytes);
            os.close();
        }
    }
}
