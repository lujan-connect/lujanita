# Contratos MCP - Lujanita

**Artifact Type**: Especificaciones de contratos MCP con Odoo  
**SDD Phase**: Planificación e implementación  
**Audience**: AI Agents (principal), equipo de desarrollo (referencia)

---

## Propósito

Define los **contratos del Model Context Protocol (MCP)** entre el middleware Java (cliente) y el servidor MCP de Odoo. Estos contratos garantizan interoperabilidad y versionado consistente.

---

## Estructura de Contratos

Todos los contratos viven en `packages/contracts/src/` y deben tener:
- **TypeScript**: para el widget (tipos de request/response)
- **Java**: para el middleware (records o clases)
- **Mock data**: en `packages/contracts/mocks/` para testing

---

## Contrato: Orders

### Operación: `orders.get`

**Descripción**: Obtiene detalles de una orden de venta desde Odoo.

**Request**:
```typescript
// packages/contracts/src/mcp/OrdersGetRequest.ts
export interface OrdersGetRequest {
  orderId: string;
  includeLines?: boolean;
}
```

```java
// apps/middleware/src/main/java/.../contracts/OrdersGetRequest.java
public record OrdersGetRequest(
    @NotBlank String orderId,
    Boolean includeLines
) {}
```

**Response**:
```typescript
// packages/contracts/src/mcp/OrdersGetResponse.ts
export interface OrdersGetResponse {
  orderId: string;
  status: 'draft' | 'confirmed' | 'done' | 'cancel';
  customerName: string;
  totalAmount: number;
  createdAt: string; // ISO 8601
  lines?: OrderLine[];
}

export interface OrderLine {
  productId: string;
  productName: string;
  quantity: number;
  price: number;
}
```

```java
// apps/middleware/src/main/java/.../contracts/OrdersGetResponse.java
public record OrdersGetResponse(
    @NotBlank String orderId,
    @NotBlank String status,
    @NotBlank String customerName,
    @NotNull BigDecimal totalAmount,
    @NotBlank String createdAt,
    List<OrderLine> lines
) {}

public record OrderLine(
    @NotBlank String productId,
    @NotBlank String productName,
    @NotNull Integer quantity,
    @NotNull BigDecimal price
) {}
```

**Modelo Odoo**: `sale.order`

**Mock**:
```json
// packages/contracts/mocks/orders.get.success.json
{
  "orderId": "SO001",
  "status": "confirmed",
  "customerName": "Juan Pérez",
  "totalAmount": 1500.00,
  "createdAt": "2025-11-29T10:30:00Z",
  "lines": [
    {
      "productId": "PROD-001",
      "productName": "Vino Malbec Premium",
      "quantity": 6,
      "price": 250.00
    }
  ]
}
```

---

## Contrato: Customers

### Operación: `customers.search`

**Descripción**: Busca clientes por nombre, email o teléfono.

**Request**:
```typescript
export interface CustomersSearchRequest {
  query: string;
  limit?: number;
}
```

**Response**:
```typescript
export interface CustomersSearchResponse {
  customers: Customer[];
  totalCount: number;
}

export interface Customer {
  customerId: string;
  name: string;
  email: string;
  phone?: string;
}
```

**Modelo Odoo**: `res.partner`

---

## Contrato: Products

### Operación: `products.list`

**Descripción**: Lista productos disponibles en catálogo.

**Request**:
```typescript
export interface ProductsListRequest {
  categoryId?: string;
  available?: boolean;
  limit?: number;
  offset?: number;
}
```

**Response**:
```typescript
export interface ProductsListResponse {
  products: Product[];
  totalCount: number;
}

export interface Product {
  productId: string;
  name: string;
  description?: string;
  price: number;
  stock: number;
  categoryId: string;
}
```

**Modelo Odoo**: `product.product`

---

## Contrato: Deliveries

### Operación: `deliveries.track`

**Descripción**: Rastrea el estado de una entrega.

**Request**:
```typescript
export interface DeliveriesTrackRequest {
  deliveryId: string;
}
```

**Response**:
```typescript
export interface DeliveriesTrackResponse {
  deliveryId: string;
  orderId: string;
  status: 'pending' | 'in_transit' | 'delivered' | 'failed';
  estimatedDelivery?: string;
  actualDelivery?: string;
  trackingEvents: TrackingEvent[];
}

export interface TrackingEvent {
  timestamp: string;
  status: string;
  location?: string;
  notes?: string;
}
```

**Modelo Odoo**: `stock.picking`

---

## Versionado de Contratos

- **Semantic Versioning**: Cada contrato tiene versión en `packages/contracts/package.json` (TypeScript) y `build.gradle` (Java).
- **Breaking Changes**: Cambios en campos obligatorios o tipos requieren MAJOR bump.
- **Backward Compatible**: Agregar campos opcionales o nuevas operaciones requiere MINOR bump.
- **Deprecación**: Marcar operaciones obsoletas con `@deprecated` y mantener por 2 releases.

---

## Agregar un Nuevo Contrato

1. Crear spec en `/specs/NNN-feature/data-model.md`
2. Definir interfaces TypeScript en `packages/contracts/src/mcp/`
3. Definir records Java en `apps/middleware/src/.../contracts/`
4. Agregar mocks en `packages/contracts/mocks/`
5. Escribir tests de contrato:
   - Widget: validar que el tipo TypeScript es correcto
   - Middleware: validar que el record Java se serializa/deserializa correctamente
6. Documentar en este archivo
7. Actualizar `packages/contracts/CHANGELOG.md`

---

## Contratos con Ollama

El middleware se integra con Ollama para capacidades de lenguaje natural. Estos contratos son internos del middleware (no expuestos vía MCP).

### Operación: `ollama.chat`

**Descripción**: Envía un mensaje al modelo LLM y recibe una respuesta conversacional.

**Request**:
```java
// apps/middleware/src/main/java/.../ollama/OllamaChatRequest.java
public record OllamaChatRequest(
    @NotBlank String model,        // e.g., "tinyllama", "phi-2"
    @NotNull List<Message> messages,
    @NotNull ChatOptions options
) {}

public record Message(
    @NotBlank String role,         // "system", "user", "assistant"
    @NotBlank String content
) {}

public record ChatOptions(
    Double temperature,            // 0.0 - 2.0
    Integer maxTokens,
    Boolean stream
) {}
```

**Response**:
```java
public record OllamaChatResponse(
    @NotBlank String model,
    @NotBlank String response,
    Boolean done,
    ChatMetrics metrics
) {}

public record ChatMetrics(
    Integer promptTokens,
    Integer completionTokens,
    Long durationMs
) {}
```

**Modelo Ollama**: `tinyllama` (por defecto) o `phi-2`

**Mock**:
```json
// packages/contracts/mocks/ollama.chat.success.json
{
  "model": "tinyllama",
  "response": "Entiendo que necesitas información sobre tu pedido SO001. Déjame consultarlo...",
  "done": true,
  "metrics": {
    "promptTokens": 45,
    "completionTokens": 23,
    "durationMs": 1250
  }
}
```

### Operación: `ollama.embed`

**Descripción**: Genera embeddings de texto para búsqueda semántica.

**Request**:
```java
public record OllamaEmbedRequest(
    @NotBlank String model,        // e.g., "nomic-embed-text"
    @NotBlank String text
) {}
```

**Response**:
```java
public record OllamaEmbedResponse(
    @NotNull List<Double> embedding,
    Integer dimensions
) {}
```

---

## Códigos de Error MCP

### Errores de Odoo

| Código | Significado | Acción del Middleware |
|--------|-------------|----------------------|
| `NOT_FOUND` | Recurso no existe en Odoo | Retornar 404 al widget |
| `INVALID_PARAMS` | Parámetros inválidos | Retornar 400 con detalles |
| `UNAUTHORIZED` | Sin permisos en Odoo | Retornar 403 |
| `TIMEOUT` | Odoo no respondió a tiempo | Retornar 504, registrar alerta |
| `INTERNAL_ERROR` | Error inesperado en Odoo | Retornar 502, escalar |

### Errores de Ollama

| Código | Significado | Acción del Middleware |
|--------|-------------|----------------------|
| `MODEL_NOT_FOUND` | Modelo no disponible en Ollama | Retornar 503, usar fallback |
| `OLLAMA_TIMEOUT` | Ollama no respondió | Retornar 504, registrar alerta |
| `CONTEXT_LENGTH_EXCEEDED` | Prompt muy largo | Retornar 413, truncar contexto |
| `OLLAMA_UNAVAILABLE` | Servicio Ollama offline | Retornar 503, modo degradado |

---

Mantén este archivo sincronizado cada vez que se agregue o modifique una operación MCP. Es la fuente de verdad para contratos entre el middleware y Odoo.

