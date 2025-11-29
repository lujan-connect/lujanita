---
title: 002 - Plan de Implementación BFF API REST
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [bff, api, rest, java, springboot, plan]
---

# Plan de Implementación - 002 BFF API REST

## Resumen
Implementar un BFF en Spring Boot (Java 21 + Maven) que provea:
- `POST /api/chat` (con intent/entities opcionales)
- `GET /api/orders/{id}` (resumen básico)
- `GET /health` (estado y componentes)

Con seguridad por headers (`apiKey`, `role`, `profile`), trazabilidad (`correlationId`), y rate limiting (60 req/min por apiKey).

---

## Arquitectura Técnica

- Runtime: Java 21 + Spring Boot 3.2 + Maven
- Estructura propuesta:
```
apps/middleware/
  src/main/java/com/lujanita/bff/
    BffApplication.java
    config/
      WebConfig.java           # filtros, CORS, rate limit
      ObservabilityConfig.java # logging estructurado
    controller/
      ChatController.java
      OrdersController.java
      HealthController.java
    service/
      ChatService.java          # orquestación con ollama + MCP
      OrdersService.java        # consulta MCP
      HealthService.java        # chequeos componentes
    client/
      llm/OllamaClient.java
      mcp/OdooMcpClient.java
    model/
      dto/* (ChatRequest/Response, OrderSummary, HealthStatus)
    util/
      CorrelationId.java, ErrorCodes.java
  src/test/java/... (BDD + unit)
```

- Configuración:
  - `application.yml`: endpoints MCP/Ollama, timeouts, modelo por defecto, rate limit
  - Beans para clientes (`WebClient`, `RestTemplate` opcional)

---

## Endpoints y Contratos

### 1) POST /api/chat
- Request headers: `apiKey`, `role`, `profile`, `correlationId`
- Request body: `{ message: string, sessionId?: string }`
- Response body: `{ response: string, correlationId: string, intent?: string, entities?: Record<string,string> }`
- Errores: mapear a `MW00X` con detalles

Flujo:
1. Validar headers (no nulos, formatos básicos)
2. Generar/propagar `correlationId`
3. Opcional: invocar `ollama-client` para clasificar intención
4. Si requiere datos → invocar `odoo-mcp-client`
5. Componer respuesta e incluir `intent`/`entities` si aplica
6. Registrar logs (json) con latencias y estado

### 2) GET /api/orders/{id}
- Response: `{ orderId, status, totalAmount, customerName }`
- Errores: `OD00X` mapeados a `MW00X` con mensaje claro

Flujo:
1. Validar `id`
2. Llamar `odoo-mcp-client.orders.get`
3. Transformar a `OrderSummary`
4. Responder 200 o error mapeado

### 3) GET /health
- Response: `{ status, version, components: { ollama: { status, model }, odoo: { status, latencyMs } } }`

Flujo:
1. Chequear conectividad a Ollama (ping/chat rápido, `stream=false`)
2. Chequear MCP Odoo (request simple con timeout bajo)
3. Construir respuesta y medir latencias

---

## Seguridad

- Autenticación por headers: `apiKey`, `role`, `profile`
- Validar presencia y formato; rechazar 401 si faltan
- Autorización básica: 403 si `role` no habilitado para operación
- No persistir `apiKey` en storage; evitar exposición en logs (hash en logs si se necesita)

## Autorización basada en apiKey/role/profile

Política:
- Toda solicitud al BFF debe incluir headers:
  - `apiKey`: identifica al cliente habilitado
  - `role`: determina permisos (p.ej., `guest`, `agent`, `admin`)
  - `profile`: selecciona contexto/segmento de operación (p.ej., `default`, `internal`)

Validación de headers:
- Presencia obligatoria de `apiKey` y `role`; `profile` por defecto `default` si no se provee
- Formato:
  - `apiKey`: string no vacío (sin espacios), longitud 20-100
  - `role`: enum conocido (`guest|agent|admin`)
  - `profile`: slug (`[a-z0-9-]+`)
- Errores:
  - Falta de `apiKey` → 401 `{ code: 'MW002', message: 'missingApiKey' }`
  - `role` no permitido para operación → 403 `{ code: 'MW003', message: 'forbiddenRole' }`
  - `profile` inválido → 400 `{ code: 'MW001', message: 'invalidProfile' }`

Autorización por endpoint:
- `POST /api/chat`: roles permitidos `guest`, `agent`, `admin`
- `GET /api/orders/{id}`: roles permitidos `agent`, `admin`
- `GET /health`: roles permitidos `admin` (lectura interna) y `agent` (limitado)

Propagación de headers a clientes:
- A `odoo-mcp-client`: `X-Api-Key`, `X-Role`, `X-Profile`
- A `ollama-client`: incluye `correlationId` y metadatos de `role`/`profile` en logs

Diseño del filtro (Request Filter):
- Orden: `SecurityHeadersFilter` ejecuta antes de controladores
- Responsabilidades:
  - Validar headers y normalizar valores
  - Rechazar temprano (401/403/400) con códigos `MW00X`
  - Inyectar `correlationId` en contexto (MDC/ThreadLocal)
  - Aplicar rate limit por `apiKey`

Auditoría y trazabilidad:
- Registrar `{ correlationId, apiKeyHash, role, profile, operation, result }`
- `apiKeyHash`: SHA-256 del `apiKey` para evitar exponer el valor

## Rate Limiting

- Política: 60 req/min por `apiKey`
- Implementación: filtro de rate limit (in-memory bucket por ahora)
- Respuesta excedente: 429 `{ code: 'MW007', message: 'rateLimitExceeded' }`

## Observabilidad

- Log JSON por request:
  - `{ timestamp, correlationId, operation, status, durationMs, extras }`
- Códigos de error:
  - `MW00X` (BFF), `LLM00X` (Ollama), `OD00X` (MCP)
- Métricas (futuro): contador por endpoint, latencia p95/p99

---

## DTOs (borrador)

- ChatRequest: `{ message: string, sessionId?: string }`
- ChatResponse: `{ response: string, correlationId: string, intent?: string, entities?: Record<string,string> }`
- OrderSummary: `{ orderId: string, status: string, totalAmount: number, customerName: string }`
- HealthStatus: `{ status: 'UP'|'DOWN', version: string, components: { ollama: { status: string, model: string }, odoo: { status: string, latencyMs: number } } }`

---

## Testing (Test-First)

- BDD (Cucumber): features en `apps/middleware/features/`
  - `bff_chat_api.feature`
  - `bff_orders_api.feature`
  - `bff_health_api.feature`
- Unit: JUnit5 + Mockito
- Integración: WireMock para MCP/Ollama si aplica

Secuencia:
1. Ejecutar BDD (rojo) → steps pendientes
2. Implementar mínima lógica en servicios/clientes
3. Ejecutar BDD (verde)
4. Añadir tests unitarios para ramas de error

---

## Pasos Concretos (Iteraciones)

- Iteración 1: Health
  - Implementar `HealthController` + `HealthService`
  - Cliente básico para ping a Ollama y MCP
  - Logs y estructura de respuesta

- Iteración 2: Orders
  - Implementar `OrdersController` + `OrdersService`
  - Cliente MCP `orders.get` y mapeo a `OrderSummary`
  - Manejo de errores `OD00X` → `MW00X`

- Iteración 3: Chat
  - Implementar `ChatController` + `ChatService`
  - Invocar `ollama-client` para respuesta breve/intención
  - Integrar `odoo-mcp-client` cuando el intent requiera datos
  - Respuesta enriquecida y logging

- Iteración 4: Seguridad y Rate limit
  - Filtro de headers (auth básica)
  - Filtro de rate limiting (por `apiKey`)

---

## Configuración y Variables

- `BFF_PORT=9000`
- `OLLAMA_HOST=http://localhost:11434`
- `OLLAMA_MODEL=tinyllama`
- `ODOO_MCP_URL=http://localhost:8069/mcp`
- `BFF_RATE_LIMIT_PER_MIN=60`
- Timeouts: `OLLAMA_TIMEOUT=30000`, `MCP_TIMEOUT=15000`

---

## Riesgos & Mitigaciones

- Latencia con CPU-only → limitar `maxTokens`, timeouts adecuados
- Errores MCP/Ollama → mapeo claro y mensajes amigables
- Seguridad por headers → posible fortalecimiento futuro (JWT/OAuth)

---

## Gate de Calidad del Plan

- [x] Alineado con `spec.md` (endpoints, headers, rate limit, health)
- [x] Contratos definidos y trazados
- [x] Secuencia test-first clara
- [x] Observabilidad y errores especificados

## Próximos Pasos

1) Ejecutar `speckit.gherkin` (ya creado) y correr BDD (rojo)
2) Implementar Iteración 1 (Health) hasta verde
3) Continuar con Orders y Chat
4) Documentar patrones nuevos en `.github/copilot-knowledge/`

## Orquestación conversacional (BFF ↔ LLM ↔ MCP)

Objetivo: El BFF decide cuándo y cómo invocar el LLM (Ollama) y el MCP (Odoo) para responder al frontend, respetando seguridad y trazabilidad.

Flujos base:
- Consulta informativa (solo LLM):
  1) Validar headers (apiKey/role/profile) y `correlationId`
  2) Invocar `ollama-client.chat(messages, options)`
  3) Componer `ChatResponse` con `response`, `intent?`, `entities?`

- Consulta de datos (LLM + MCP):
  1) Validar headers y `correlationId`
  2) (Opcional) LLM clasifica intención y extrae entidades (p.ej., `orderId`)
  3) Invocar `odoo-mcp-client.orders.get(orderId)` con headers propagados
  4) Componer respuesta final (texto + resumen de orden) y registrar métricas

Políticas:
- Fallback: si LLM falla → usar respuesta corta de sistema y sugerir reintento
- Tiempo máximo por operación: respetar `OLLAMA_TIMEOUT` y `MCP_TIMEOUT`
- Observabilidad: registrar `operation`, `correlationId`, `llmOperation`, `mcpOperation`, latencias

## Autenticación downstream (Ollama y MCP)

Requisitos:
- Hacia Ollama:
  - No requiere `apiKey` por defecto, pero el BFF debe incluir `correlationId` y metadatos (`role`, `profile`) en logs/headers internos si aplica
  - Respetar modelo configurado (`OLLAMA_MODEL`) y opciones v1
- Hacia MCP Odoo:
  - Propagar `X-Api-Key`, `X-Role`, `X-Profile` desde el BFF
  - Si MCP requiere credenciales propias (token/usuario): configurar `MCP_TOKEN`/`MCP_CLIENT_ID` en `application.yml` y enviar en header `Authorization: Bearer <token>`

Validaciones y errores:
- Credenciales faltantes hacia MCP → responder 401 `MW002` y registrar `OD00X` interno si aplica
- Token expirado/invalidado → mapear 403 `MW003` y detallar causa
- Desalineación de `role/profile` entre BFF y MCP → 400 `MW001` (perfil inválido) o 403 según política

Propagación de contexto:
- Incluir `correlationId` en todas las llamadas (LLM/MCP) para traceo end-to-end
- Auditar `apiKeyHash`, `role`, `profile` por operación de orquestación
