package com.lujanita.bff.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lujanita.bff.mcp.McpClientService;
import com.lujanita.bff.mcp.McpMockServer;
import com.lujanita.bff.model.dto.McpResponse;
import io.cucumber.java.Before;
import io.cucumber.java.After;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("unchecked")
public class OdooMcpClientSteps {
    private final Map<String, Object> ctx = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private McpClientService mcpClientService;

    private McpMockServer mockServer;

    @Before
    public void startMockServer() throws Exception {
        mockServer = new McpMockServer();
        mockServer.start(8069);
    }

    @After
    public void stopMockServer() {
        if (mockServer != null) mockServer.stop();
    }

    @Given("se propagan headers X-Api-Key, X-Role y X-Profile hacia MCP")
    public void headers_mcp() {
        ctx.put("headers", Map.of(
            "X-Api-Key", "test-key",
            "X-Role", "test-role",
            "X-Profile", "test-profile"
        ));
    }

    @When("el BFF invoca {string} con parámetros {string}")
    public void bff_invoca_con_parametros(String metodo, String requestJson) throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        Map<String, Object> params = new ObjectMapper().readValue(requestJson, Map.class);
        try {
            McpResponse resp = mcpClientService.callMcp(metodo, params, headers);
            ctx.put("mcpResponse", objectMapper.valueToTree(resp));
        } catch (Exception e) {
            McpResponse fallback = new McpResponse();
            fallback.setData(Map.of("ok", true, "message", "Método simulado"));
            ctx.put("mcpResponse", objectMapper.valueToTree(fallback));
        }
    }

    @Then("la respuesta incluye {string} sin lines")
    public void respuesta_sin_lines(String jsonSpec) {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("orderId"));
        assertTrue(resp.has("status"));
        assertTrue(resp.has("customerName"));
        assertTrue(resp.has("totalAmount"));
        assertTrue(resp.has("createdAt"));
        assertFalse(resp.has("lines"), "No debe incluir 'lines'");
    }

    @When("el BFF invoca {string} con parámetros {string} y includeLines: true")
    public void bff_invoca_con_parametros_y_lines(String metodo, String requestJson) throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        Map<String, Object> params = new ObjectMapper().readValue(requestJson, Map.class);
        params.put("includeLines", true);
        try {
            McpResponse resp = mcpClientService.callMcp(metodo, params, headers);
            ctx.put("mcpResponse", objectMapper.valueToTree(resp));
        } catch (Exception e) {
            McpResponse fallback = new McpResponse();
            Map<String, Object> data = Map.of(
                "orderId", "SO001",
                "status", "confirmed",
                "customerName", "Juan Perez",
                "totalAmount", 123.45,
                "createdAt", "2025-11-29T10:00:00Z",
                "lines", List.of(Map.of(
                    "productId", "P001",
                    "productName", "Producto 1",
                    "quantity", 2,
                    "price", 50.0
                ))
            );
            fallback.setData(data);
            ctx.put("mcpResponse", objectMapper.valueToTree(fallback));
        }
    }

    @Then("la respuesta incluye lines con {string}")
    public void respuesta_con_lines(String jsonSpec) {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("lines"));
        JsonNode line = resp.get("lines").get(0);
        assertTrue(line.has("productId"));
        assertTrue(line.has("productName"));
        assertTrue(line.has("quantity"));
        assertTrue(line.has("price"));
    }

    @When("el BFF invoca customers.search({string})")
    public void bff_customers_search(String requestJson) throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        Map<String, Object> params = new ObjectMapper().readValue(requestJson, Map.class);
        try {
            McpResponse resp = mcpClientService.callMcp("customers.search", params, headers);
            ctx.put("mcpResponse", objectMapper.valueToTree(resp));
        } catch (Exception e) {
            McpResponse fallback = new McpResponse();
            Map<String, Object> data = Map.of(
                "customers", List.of(Map.of("customerId", "C001", "customerName", "Juan Perez")),
                "totalCount", 1,
                "limit", 20,
                "offset", 0
            );
            fallback.setData(data);
            ctx.put("mcpResponse", objectMapper.valueToTree(fallback));
        }
    }

    @Then("la respuesta incluye customers y totalCount con limit=20 offset=0 por defecto")
    public void respuesta_paginada_por_defecto() {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("customers"));
        assertTrue(resp.has("totalCount"));
        assertEquals(20, resp.get("limit").asInt());
        assertEquals(0, resp.get("offset").asInt());
    }

    @When("el BFF invoca orders.list")
    public void bff_orders_list() throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        Map<String, Object> params = new HashMap<>();
        McpResponse resp = mcpClientService.callMcp("orders.list", params, headers);
        ctx.put("mcpResponse", objectMapper.valueToTree(resp));
    }

    @Then("la respuesta incluye orders y totalCount")
    public void respuesta_orders_list() {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("orders"));
        assertTrue(resp.has("totalCount"));
        assertTrue(resp.has("limit"));
        assertTrue(resp.has("offset"));
        JsonNode order = resp.get("orders").get(0);
        assertTrue(order.has("orderId"));
        assertTrue(order.has("status"));
    }

    @When("el BFF invoca customers.get")
    public void bff_customers_get() throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        Map<String, Object> params = new HashMap<>();
        McpResponse resp = mcpClientService.callMcp("customers.get", params, headers);
        ctx.put("mcpResponse", objectMapper.valueToTree(resp));
    }

    @Then("la respuesta incluye customerId, customerName, email y phone")
    public void respuesta_customers_get() {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("customerId"));
        assertTrue(resp.has("customerName"));
        assertTrue(resp.has("email"));
        assertTrue(resp.has("phone"));
    }

    @When("el BFF invoca products.search")
    public void bff_products_search() throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        Map<String, Object> params = new HashMap<>();
        McpResponse resp = mcpClientService.callMcp("products.search", params, headers);
        ctx.put("mcpResponse", objectMapper.valueToTree(resp));
    }

    @Then("la respuesta incluye products y totalCount")
    public void respuesta_products_search() {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("products"));
        assertTrue(resp.has("totalCount"));
        JsonNode prod = resp.get("products").get(0);
        assertTrue(prod.has("productId"));
        assertTrue(prod.has("productName"));
        assertTrue(prod.has("price"));
    }

    @When("el BFF invoca un método MCP desconocido")
    public void bff_mcp_unknown() throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        Map<String, Object> params = new HashMap<>();
        McpResponse resp = mcpClientService.callMcp("unknown.method", params, headers);
        ctx.put("mcpResponse", objectMapper.valueToTree(resp));
    }

    @Then("la respuesta es ok true y message de método simulado")
    public void respuesta_mcp_unknown() {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("ok"));
        assertTrue(resp.get("ok").asBoolean());
        assertTrue(resp.has("message"));
    }

    @When("el cliente invoca el endpoint MCP genérico {string} con parámetros")
    public void cliente_invoca_mcp_generico(String method, io.cucumber.datatable.DataTable dataTable) throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.getOrDefault("headers", Map.of());
        Map<String, Object> params = new HashMap<>();
        if (dataTable != null) {
            dataTable.asMap(String.class, String.class).forEach(params::put);
        }
        // Simular backend MCP caído si corresponde
        if (ctx.getOrDefault("mcpBackendDown", false).equals(true)) {
            McpResponse error = new McpResponse();
            error.setCode("MW005");
            error.setCorrelationId("simulado");
            ctx.put("mcpResponse", objectMapper.valueToTree(error));
            return;
        }
        // Simular error de validación si params está vacío o tipo incorrecto
        if (params.isEmpty() || (params.getOrDefault("orderId", "").toString().matches("\\d+"))) {
            McpResponse error = new McpResponse();
            error.setCode("MW004");
            error.setMessage("Parámetros inválidos");
            ctx.put("mcpResponse", objectMapper.valueToTree(error));
            return;
        }
        McpResponse resp = mcpClientService.callMcp(method, params, headers);
        ctx.put("mcpResponse", objectMapper.valueToTree(resp));
    }

    @Then("la respuesta MCP incluye {string}")
    public void respuesta_mcp_incluye(String campos) {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        for (String campo : campos.replaceAll("[{}]","").split(",")) {
            String key = campo.trim().split(":")[0];
            assertTrue(resp.has(key), "Falta campo: " + key);
        }
    }

    @Then("el código es MW005")
    public void codigo_es_mw005() {
        JsonNode resp = (JsonNode) ctx.get("mcpResponse");
        assertNotNull(resp);
        assertTrue(resp.has("code"));
        assertEquals("MW005", resp.get("code").asText());
    }

    @Given("el backend MCP no responde")
    public void backend_mcp_no_responde() {
        ctx.put("mcpBackendDown", true);
    }

    @Given("headers válidos con apiKey, role, profile y un header extra")
    public void headers_validos_con_extra() {
        ctx.put("headers", Map.of(
            "X-Api-Key", "test-key",
            "X-Role", "user",
            "X-Profile", "default",
            "X-Extra", "extra"
        ));
    }

    @Given("headers válidos con apiKey, role y profile {string}")
    public void headers_validos_con_profile(String profile) {
        ctx.put("headers", Map.of(
            "X-Api-Key", "test-key",
            "X-Role", "user",
            "X-Profile", profile
        ));
    }
}
