# Middleware API Patterns - SDD Knowledge Base

**Artifact Type**: Patrones técnicos para generación de código Java Spring Boot  
**SDD Phase**: Implementación  
**Audience**: AI Agents (principal), equipo de desarrollo (referencia)

---

## Propósito

Este artefacto provee patrones estructurados para asistentes AI que generan código del **middleware Java** encargado de integrar el widget Lujanita con el servidor MCP de Odoo. Úsalo para implementar endpoints REST, clientes MCP y adaptadores documentados en los specs.

---

## Stack Operativo
- **Runtime**: Java 21 + Spring Boot 3.x.
- **Infra**: VM dedicada en Google Cloud Run jobs/VMs con despliegue empaquetado en contenedor.
- **Inference**: Servidor Ollama embebido (modelo ligero, p.ej. `llama3.2-instruct`) expuesto en `http://127.0.0.1:11434` dentro de la misma VM.
- **Responsabilidad**: El middleware orquesta llamadas MCP y delega tasks de NLP/resumen al modelo local para garantizar baja latencia y privacidad.

---

## Patrón: Crear un Nuevo Endpoint REST

### Contexto
Cuando el middleware necesita exponer una operación consumida por el widget (p.ej. `/specs/NNN-feature/spec.md`).

### Prerrequisitos
- [ ] Spec en `/specs/NNN-feature/spec.md`
- [ ] Contrato MCP documentado en `packages/contracts`
- [ ] Tarea definida en `/specs/NNN-feature/tasks.md`

### Pasos

#### 1. Definir DTOs (Request/Response)

**Ubicación**: `apps/middleware/src/main/java/com/lujanita/contracts/`
```java
@Schema(description = "Petición para obtener detalles de una orden MCP")
public record OrderDetailsRequest(
    @NotBlank String orderId,
    @NotBlank String customerId
) {}

@Schema(description = "Respuesta para una orden MCP")
public record OrderDetailsResponse(
    @NotBlank String orderId,
    @NotBlank String status,
    @Schema(description = "Timestamp ISO 8601")
    @NotBlank String lastUpdated,
    Map<String, Object> payload
) {}
```

#### 2. Crear Test (DEBE FALLAR)

**Ubicación**: `apps/middleware/src/test/java/.../OrderControllerTest.java`
```java
@AutoConfigureMockMvc
class OrderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldReturn404WhenOrderMissing() throws Exception {
    mockMvc.perform(get("/api/orders/{orderId}", "unknown"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("OD001"));
  }
}
```
Ejecutar:
```bash
cd apps/middleware && ./mvnw test --tests *OrderControllerTest
# Debe fallar porque el endpoint aún no existe
```

#### 3. Implementar Controller

```java
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping("/{orderId}")
  public ResponseEntity<OrderDetailsResponse> getOrder(@PathVariable String orderId) {
    return ResponseEntity.ok(orderService.getOrder(orderId));
  }
}
```

#### 4. Servicio + Cliente MCP

```java
@Service
@RequiredArgsConstructor
public class OrderService {

  private final OdooMcpClient odooMcpClient;

  public OrderDetailsResponse getOrder(String orderId) {
    return odooMcpClient.fetchOrder(orderId)
      .orElseThrow(() -> new DomainException("OD001", "Orden no encontrada"));
  }
}
```

#### 5. Registrar Cliente MCP
- Configurar bean MCP en `apps/middleware/src/main/java/.../config/McpConfig.java`
- Usar contratos de `packages/contracts`

#### 6. Re-ejecutar Tests
```bash
cd apps/middleware && ./mvnw test --tests *OrderControllerTest
# Debe pasar
```

---

## Patrón: Cliente MCP para Odoo

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class OdooMcpClient {

  private final McpTransport transport;
  private final ObjectMapper mapper;

  public Optional<OrderDetailsResponse> fetchOrder(String orderId) {
    McpRequest request = McpRequest.builder()
        .operation("orders.get")
        .params(Map.of("orderId", orderId))
        .build();

    McpResponse response = transport.execute(request);
    if (response.status() == McpStatus.NOT_FOUND) {
      return Optional.empty();
    }

    return Optional.of(mapper.convertValue(response.payload(), OrderDetailsResponse.class));
  }
}
```

**Reglas**:
- Los nombres de operación MCP siguen el formato `<dominio>.<acción>` (ej: `orders.sync`)
- Manejar códigos MCP (`NOT_FOUND`, `ERROR`, `SUCCESS`)
- Convertir payloads usando los DTOs de `packages/contracts`

---

## Patrón: Manejo de Errores con Códigos

```java
@RestControllerAdvice
public class ErrorHandler {

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
    return ResponseEntity.status(ex.status())
        .body(new ErrorResponse(ex.code(), ex.getMessage()));
  }
}
```

| Código | HTTP | Uso |
|--------|------|-----|
| `OD001` | 404 | Orden no encontrada en Odoo |
| `MW001` | 502 | Error comunicándose con MCP |
| `UI001` | 400 | Payload inválido desde el widget |

---

## Patrón: Métricas y Logs

```java
@Slf4j
@Service
public class ChatSessionService {

  private final MeterRegistry registry;

  public ChatSession startSession(ChatSessionRequest request) {
    long start = System.currentTimeMillis();
    try {
      ChatSession session = // ... lógica ...
      registry.counter("chat.sessions", "status", "success").increment();
      return session;
    } catch (Exception ex) {
      registry.counter("chat.sessions", "status", "error").increment();
      throw ex;
    } finally {
      long duration = System.currentTimeMillis() - start;
      registry.timer("chat.sessions.latency").record(duration, TimeUnit.MILLISECONDS);
      log.info("chat_session_started", kv("correlationId", request.correlationId()));
    }
  }
}
```

---

## Patrón: Tests de Integración MCP

```java
@SpringBootTest
class OdooMcpClientIT {

  @Autowired
  private OdooMcpClient client;

  @Test
  void shouldMapOrderResponse() {
    StepVerifier.create(client.fetchOrder("ORD-1"))
      .expectNextMatches(order -> order.status().equals("confirmed"))
      .verifyComplete();
  }
}
```

- Utilizar WireMock o servidores MCP simulados
- Contratos en `packages/contracts/mocks`

---

Mantén este archivo sincronizado con los patrones del equipo Lujanita. Si surgen nuevos flujos (ej. scheduling, atención omnicanal), documenta el patrón aquí antes de implementarlo.

---

## Patrón: Invocar el Modelo Ollama Embebido

### Contexto
Cuando el middleware requiere enriquecimiento ligero (resumen de conversación, clasificación de intención) sin depender de servicios externos. El modelo corre en la misma VM mediante Ollama.

### Configuración del Cliente

```java
@Configuration
public class OllamaConfig {

  @Bean
  WebClient ollamaWebClient(WebClient.Builder builder) {
    return builder
        .baseUrl("http://127.0.0.1:11434")
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
```

### Servicio de Inferencia

```java
@Service
@RequiredArgsConstructor
public class IntentClassifier {

  private final WebClient ollamaWebClient;
  @Value("${ollama.model:llama3.2-instruct}")
  private String modelName;

  public Mono<String> classify(String prompt) {
    Map<String, Object> body = Map.of(
        "model", modelName,
        "prompt", prompt,
        "stream", false
    );

    return ollamaWebClient.post()
        .uri("/api/generate")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
        .map(res -> (String) res.getOrDefault("response", ""));
  }
}
```

### Prueba Unitaria

```java
@WebFluxTest(IntentClassifier.class)
class IntentClassifierTest {

  @Autowired
  private IntentClassifier classifier;

  @Autowired
  private WebClient.Builder builder;

  @Test
  void shouldCallEmbeddedOllama() {
    MockWebServer server = new MockWebServer();
    server.enqueue(new MockResponse()
        .setBody("{\"response\":\"booking\"}")
        .addHeader("Content-Type", "application/json"));

    IntentClassifier testClassifier = new IntentClassifier(
        builder.baseUrl(server.url("/").toString()).build());

    StepVerifier.create(testClassifier.classify("classify booking intent"))
        .expectNext("booking")
        .verifyComplete();
  }
}
```

### Consideraciones
- Nunca exponer el endpoint de Ollama fuera de la VM.
- Ajustar `modelName` vía `OLLAMA_MODEL` para intercambiar variantes ligeras.
- Registrar métricas (`ollama.calls`, `latency`) para detectar degradaciones.
- Añadir circuit-breaker (Resilience4j) si se requieren límites de QPS.
