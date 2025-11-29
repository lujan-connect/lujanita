package com.lujanita.bff.mcp;

import com.lujanita.bff.config.BffProperties;
import com.lujanita.bff.model.dto.McpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class McpClientService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private BffProperties bffProperties;

    public McpResponse callMcp(String method, Map<String, Object> params, Map<String, String> headers) {
        // Normalizar headers a minúsculas por seguridad
        Map<String, String> normHeaders = new HashMap<>();
        if (headers != null) headers.forEach((k, v) -> normHeaders.put(k.toLowerCase(), v));

        // Si el mock está habilitado, interceptamos y devolvemos datos sintéticos
        if (bffProperties.getMcp() != null && bffProperties.getMcp().isMockEnabled()) {
            return buildMockResponse(method, params, normHeaders);
        }

        String endpoint = bffProperties.getMcp().getEndpoint();
        int timeout = bffProperties.getMcp().getTimeoutMs();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        Map<String, Object> body = Map.of(
            "method", method,
            "params", params
        );
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
        try {
            return objectMapper.readValue(response.getBody(), McpResponse.class);
        } catch (Exception e) {
            McpResponse error = new McpResponse();
            error.setCode("MW005");
            error.setMessage("Error deserializando respuesta MCP: " + e.getMessage());
            return error;
        }
    }

    private McpResponse buildMockResponse(String method, Map<String, Object> params, Map<String, String> headers) {
        McpResponse resp = new McpResponse();
        resp.setCode("OK");
        resp.setMessage("mock");
        resp.setCorrelationId(headers != null ? headers.getOrDefault("x-correlation-id", UUID.randomUUID().toString()) : UUID.randomUUID().toString());

        // Rutas relacionadas a presupuestos/quotes
        String m = method != null ? method.toLowerCase() : "";
        if (m.contains("quote") || m.contains("presup") || m.contains("budget") || m.contains("sale.order")) {
            if (m.endsWith(".list") || m.contains("list") || m.endsWith("s.list") || m.contains("orders.list") || m.contains("quotes.list")) {
                resp.setData(Map.of("items", syntheticQuoteList()));
                return resp;
            }
            if (m.endsWith(".get") || m.contains(".get") || m.contains("quotes.get") || m.contains("orders.get")) {
                String id = params != null && params.get("id") != null ? params.get("id").toString() : UUID.randomUUID().toString();
                resp.setData(syntheticFullQuote(id));
                return resp;
            }
            // fallback: devolver un resumen
            resp.setData(Map.of("quote", syntheticFullQuote(UUID.randomUUID().toString())));
            return resp;
        }

        // Otros métodos útiles (orders.list, orders.get)
        if (m.contains("order") || m.contains("orders")) {
            if (m.contains("list")) {
                resp.setData(Map.of("items", syntheticOrderList()));
                return resp;
            }
            if (m.contains("get")) {
                String id = params != null && params.get("orderId") != null ? params.get("orderId").toString() : UUID.randomUUID().toString();
                resp.setData(syntheticFullOrder(id));
                return resp;
            }
        }

        // Por defecto, devolver un objeto genérico
        resp.setData(Map.of("result", Map.of("note", "mocked response for method " + method)));
        return resp;
    }

    private List<Map<String, Object>> syntheticQuoteList() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            String id = "Q-" + (1000 + i);
            items.add(Map.of(
                "id", id,
                "name", "Presupuesto " + i,
                "status", i % 2 == 0 ? "confirmed" : "draft",
                "total", 100.0 * i,
                "currency", "USD",
                "date", nowIso()
            ));
        }
        return items;
    }

    private Map<String, Object> syntheticFullQuote(String id) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            lines.add(Map.of(
                "productId", "P-" + i,
                "description", "Producto ejemplo " + i,
                "qty", i,
                "unitPrice", 25.0 * i,
                "subtotal", 25.0 * i * i
            ));
        }
        Map<String, Object> quote = new HashMap<>();
        quote.put("id", id);
        quote.put("name", "Presupuesto demo " + id);
        quote.put("status", "draft");
        quote.put("date", nowIso());
        quote.put("validUntil", nowPlusDaysIso(30));
        quote.put("customer", Map.of("id", "C-100","name","Cliente Demo"));
        quote.put("lines", lines);
        quote.put("total", lines.stream().mapToDouble(l -> ((Number)((Map)l).get("subtotal")).doubleValue()).sum());
        quote.put("currency", "USD");
        return quote;
    }

    private List<Map<String, Object>> syntheticOrderList() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            items.add(Map.of(
                "orderId", "SO" + (200 + i),
                "status", i % 2 == 0 ? "confirmed" : "draft",
                "amountTotal", 250.0 * i,
                "date", nowIso()
            ));
        }
        return items;
    }

    private Map<String, Object> syntheticFullOrder(String id) {
        List<Map<String, Object>> lines = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            lines.add(Map.of(
                "productId", "P-" + i,
                "name", "Producto " + i,
                "qty", i,
                "priceUnit", 50.0 * i
            ));
        }
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", id);
        order.put("status", "confirmed");
        order.put("date", nowIso());
        order.put("customer", Map.of("id", "C-200", "name", "Cliente Pedido"));
        order.put("lines", lines);
        order.put("amountTotal", lines.stream().mapToDouble(l -> ((Number)((Map)l).getOrDefault("priceUnit", 0)).doubleValue() * ((Number)((Map)l).getOrDefault("qty", 1)).doubleValue()).sum());
        order.put("currency", "USD");
        return order;
    }

    private String nowIso() {
        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(Instant.now());
    }

    private String nowPlusDaysIso(int days) {
        return DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(Instant.now().plusSeconds(days * 86400L));
    }
}
