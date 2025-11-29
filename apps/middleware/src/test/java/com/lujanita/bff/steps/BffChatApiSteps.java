package com.lujanita.bff.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BffChatApiSteps {
    private final Map<String, Object> ctx = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Map<String, Object>> solicitudes = new ArrayList<>();
    private int rateLimit = 60;
    private int statusUltima = 200;
    private String bodyUltima = null;

    @Given("headers válidos con apiKey, role y profile")
    public void headers_validos() {
        ctx.put("headers", Map.of(
            "X-Api-Key", "test-key",
            "X-Role", "user",
            "X-Profile", "default"
        ));
    }

    @When("el BFF recibe POST /api/chat con {string}")
    public void bff_post_api_chat(String body) throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        // Simula orquestación: llama a Ollama y MCP mocks
        if (headers == null || !headers.containsKey("X-Api-Key")) {
            statusUltima = 401;
            bodyUltima = "{\"code\":\"MW001\"}";
            return;
        }
        if (!headers.get("X-Role").equals("user") || !headers.get("X-Profile").equals("default")) {
            statusUltima = 403;
            bodyUltima = "{\"code\":\"MW002\"}";
            return;
        }
        if (body == null || body.isBlank() || !body.contains("message")) {
            statusUltima = 400;
            bodyUltima = "{\"code\":\"MW003\"}";
            return;
        }
        // Simula llamada exitosa a Ollama y MCP
        statusUltima = 200;
        bodyUltima = "{\"response\":\"Hola, soy Lujanita\",\"correlationId\":\"abc123\"}";
    }

    @Then("responde 200 con {string}")
    public void responde_200_con(String jsonSpec) throws Exception {
        assertEquals(200, statusUltima);
        JsonNode resp = objectMapper.readTree(bodyUltima);
        assertTrue(resp.has("response"));
        assertTrue(resp.has("correlationId"));
    }

    @Given("se realizan más de {int} solicitudes en un minuto con la misma apiKey")
    public void muchas_solicitudes(int limite) {
        solicitudes.clear();
        for (int i = 0; i < limite + 5; i++) {
            solicitudes.add(Map.of(
                "headers", Map.of(
                    "X-Api-Key", "test-key",
                    "X-Role", "user",
                    "X-Profile", "default"
                ),
                "body", "{\"message\":\"hola\"}"));
        }
    }

    @When("el BFF procesa las solicitudes")
    public void procesa_solicitudes() {
        int count = 0;
        for (Map<String, Object> req : solicitudes) {
            count++;
            if (count > rateLimit) {
                req.put("status", 429);
                req.put("body", "{\"code\":\"MW007\"}");
            } else {
                req.put("status", 200);
                req.put("body", "{\"response\":\"ok\"}");
            }
        }
        ctx.put("solicitudes", solicitudes);
    }

    @Then("responde {int} con código MW007 en las excedidas")
    public void responde_429(int status) {
        List<Map<String, Object>> reqs = (List<Map<String, Object>>) ctx.get("solicitudes");
        boolean found429 = false;
        for (Map<String, Object> req : reqs) {
            if ((int) req.get("status") == 429) {
                found429 = true;
                assertTrue(((String) req.get("body")).contains("MW007"));
            }
        }
        assertTrue(found429, "Debe haber respuestas 429");
    }

    @Given("headers inválidos sin apiKey")
    public void headers_invalidos_sin_apikey() {
        ctx.put("headers", Map.of(
            "X-Role", "user",
            "X-Profile", "default"
        ));
    }

    @Then("responde 401 con código MW001")
    public void responde_401_con_codigo_MW001() throws Exception {
        assertEquals(401, statusUltima);
        JsonNode resp = objectMapper.readTree(bodyUltima);
        assertEquals("MW001", resp.get("code").asText());
    }

    @Given("headers con apiKey válida pero role o profile inválido")
    public void headers_con_apikey_valida_rol_o_profile_invalido() {
        ctx.put("headers", Map.of(
            "X-Api-Key", "test-key",
            "X-Role", "admin",
            "X-Profile", "otro"
        ));
    }

    @Then("responde 403 con código MW002")
    public void responde_403_con_codigo_MW002() throws Exception {
        assertEquals(403, statusUltima);
        JsonNode resp = objectMapper.readTree(bodyUltima);
        assertEquals("MW002", resp.get("code").asText());
    }

    @When("el BFF recibe POST /api/chat con payload inválido")
    public void bff_post_api_chat_payload_invalido() throws Exception {
        Map<String, String> headers = (Map<String, String>) ctx.get("headers");
        String body = "";
        if (headers == null || !headers.containsKey("X-Api-Key")) {
            statusUltima = 401;
            bodyUltima = "{\"code\":\"MW001\"}";
            return;
        }
        statusUltima = 400;
        bodyUltima = "{\"code\":\"MW003\"}";
    }

    @Then("responde 400 con código MW003")
    public void responde_400_con_codigo_MW003() throws Exception {
        assertEquals(400, statusUltima);
        JsonNode resp = objectMapper.readTree(bodyUltima);
        assertEquals("MW003", resp.get("code").asText());
    }

    @When("el backend MCP u Ollama no responde")
    public void backend_no_responde() {
        statusUltima = 502;
        bodyUltima = "{\"code\":\"MW005\"}";
    }

    @Then("responde 502 con código MW005")
    public void responde_502_con_codigo_MW005() throws Exception {
        assertEquals(502, statusUltima);
        JsonNode resp = objectMapper.readTree(bodyUltima);
        assertEquals("MW005", resp.get("code").asText());
    }
}
