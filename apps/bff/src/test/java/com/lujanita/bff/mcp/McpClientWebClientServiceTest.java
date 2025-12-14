package com.lujanita.bff.mcp;

import com.lujanita.bff.model.dto.McpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class McpClientWebClientServiceTest {
    @Test
    void callMcp_returnsErrorOnException() {
        // Arrange
        BffPropertiesMock props = new BffPropertiesMock();
        WebClient.Builder builder = WebClient.builder();
        McpClientWebClientService service = new McpClientWebClientService(props, builder);
        // Simula endpoint inválido
        McpResponse resp = service.callMcp("test.method", Map.of(), Map.of()).block();
        assertNotNull(resp);
        assertEquals("MW005", resp.getCode());
        assertTrue(resp.getMessage().contains("Error MCP WebClient"));
    }

    // Mock mínimo para BffProperties
    static class BffPropertiesMock extends com.lujanita.bff.config.BffProperties {
        @Override
        public Mcp getMcp() {
            Mcp mcp = new Mcp();
            mcp.setEndpoint("http://localhost:9999/invalid");
            return mcp;
        }
    }
}
