package com.lujanita.bff.mcp;

import com.lujanita.bff.config.BffProperties;
import com.lujanita.bff.model.dto.McpResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class McpClientWebClientService {
    private final BffProperties bffProperties;
    private final WebClient.Builder webClientBuilder;

    public Mono<McpResponse> callMcp(String method, Map<String, Object> params, Map<String, String> headers) {
        String endpoint = bffProperties.getMcp().getEndpoint();
        WebClient client = webClientBuilder.baseUrl(endpoint).build();
        return client.post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .bodyValue(Map.of("method", method, "params", params))
                .retrieve()
                .bodyToMono(McpResponse.class)
                .onErrorResume(e -> Mono.just(errorResponse(e)));
    }

    private McpResponse errorResponse(Throwable e) {
        McpResponse error = new McpResponse();
        error.setCode("MW005");
        error.setMessage("Error MCP WebClient: " + e.getMessage());
        return error;
    }
}

