package com.lujanita.bff.mcp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class McpMockServer {
    private HttpServer server;

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/mcp/api", new McpHandler());
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    static class McpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{}";
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (body.contains("\"method\":\"handshake\"")) {
                exchange.getResponseHeaders().add("mcp-session-id", "mock-session-id");
            }
            // Simula el contrato MCP real del llm_mcp_server de Odoo
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
                // Fallback genérico para otros métodos
                response = "{\"ok\":true,\"message\":\"Método MCP simulado\"}";
            }
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }
}
