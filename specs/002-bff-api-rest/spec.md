---
title: 002 - BFF API REST
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [bff, api, rest, middleware, sdd]
spec_phase: specify
---

# 002 - BFF API REST

## Permalink: bff-api-rest

Diseñar un API REST en el BFF (Spring Boot) que provea funcionalidades a las apps del frontend (app mobile-first y widget embebible). La API debe ser segura, observable y con contratos claros.

## 1. Propósito y Alcance

**Propósito**: Exponer endpoints REST del BFF para conversación, órdenes, health y configuración mínima.

**Alcance**:
- In scope:
  - Endpoints públicos controlados por API key/rol/perfil
  - Conversación: `POST /api/chat`
  - Órdenes: `GET /api/orders/{id}` (consulta básica)
  - Health: `GET /health` (estado general con checks de Ollama y Odoo)
  - Observabilidad: logs estructurados y códigos de error `MW00X`
- Out of scope:
  - Lógica completa de negocio en Odoo (delegada a MCP)
  - Integración directa con bases de datos
- Future scope:
  - Streaming de respuestas (SSE/WebSocket)
  - Paginación y filtros avanzados

## 2. Definiciones

- API REST: interfaz HTTP con recursos y operaciones estándar.
- Autenticación: encabezados con `apiKey`, `role`, `profile`.
- Observabilidad: logs JSON con `correlationId`, métricas y códigos `MW00X`.

## 3. Historias de Usuario y Criterios (BDD)

### US-API-001: Enviar mensaje de chat
Como frontend necesito enviar un mensaje al BFF y recibir respuesta.
- Given headers incluyen `apiKey`, `role`, `profile`
- When se hace `POST /api/chat` con `{ message: "..." }`
- Then responde 200 con `{ response: "...", correlationId }`

### US-API-002: Consultar orden
Como frontend quiero obtener una orden por ID para mostrar su estado.
- Given existe una orden en Odoo y el BFF tiene acceso
- When `GET /api/orders/SO001`
- Then responde 200 con `orderId, status, totalAmount, customerName`

### US-API-003: Health
Como operador quiero verificar salud del servicio.
- When `GET /health`
- Then responde 200 con estado `UP/DOWN` y componentes `ollama`, `odoo`

## 4. Requisitos Funcionales (FR)

- FR-API-001: Validar headers `apiKey`, `role`, `profile` en todas las llamadas.
- FR-API-002: Generar `correlationId` por request y propagar a logs.
- FR-API-003: `POST /api/chat` acepta `{ message: string, sessionId?: string }` y retorna `{ response: string, correlationId: string, intent?: string, entities?: Record<string,string> }`.
- FR-API-004: `GET /api/orders/{id}` retorna datos mínimos de la orden según contratos MCP.
- FR-API-005: `GET /health` incluye `status`, `version`, y componentes `ollama` (status, model) y `odoo` (status, latencyMs).
- FR-API-006: Mapear errores a `MW00X` (400→MW001, 401→MW002, 403→MW003, 404→MW004, 5xx→MW005, timeout→MW006).
- FR-API-007: Respuestas JSON con camelCase.
- FR-API-008: Rate limiting por `apiKey`: 60 req/min; excedente → 429 `{ code: 'MW007', message: 'rateLimitExceeded' }`.

## 5. Criterios de Éxito

- SC-API-001: 95% de respuestas de `POST /api/chat` en < 2.5s.
- SC-API-002: Health retorna correcto estado y componentes.
- SC-API-003: Trazabilidad completa con `correlationId` en logs.
- SC-API-004: Seguridad básica: rechazar llamadas sin `apiKey`.

## 6. Entidades Clave

- ChatRequest: `{ message: string, sessionId?: string }`
- ChatResponse: `{ response: string, correlationId: string, intent?: string, entities?: Record<string,string> }`
- OrderSummary: `{ orderId: string, status: string, totalAmount: number, customerName: string }`
- HealthStatus: `{ status: 'UP'|'DOWN', version: string, components: { ollama: { status: string, model: string }, odoo: { status: string, latencyMs: number } } }`

## 7. Supuestos

- Spring Boot 3.2 con controladores REST.
- Validación y serialización estándar Jackson.
- Seguridad basada en headers simples (sin OAuth en v1).

## 8. Dependencias

- Contratos MCP (`packages/contracts`) para `orders.get`.
- Cliente Ollama para generación de respuestas.

## 9. Riesgos y Mitigaciones

- API key comprometida: rotación y monitoreo.
- Latencias altas: timeouts y retries razonables.
- Errores MCP: mapeo correcto y mensajería clara.

## 10. Escenarios de Aceptación (Resumen)

- `POST /api/chat` con headers válidos → 200 con respuesta.
- `GET /api/orders/{id}` existente → 200 con resumen.
- `GET /health` → 200 con estados de componentes y versión.

## 11. Decisiones de Clarificación

- Rate limiting v1:
  - Límite por `apiKey`: 60 solicitudes por minuto.
  - Exceso: HTTP 429 con cuerpo `{ code: 'MW007', message: 'rateLimitExceeded' }`.
- Health:
  - Respuesta incluye: `status`, `version`, `components.ollama.status`, `components.ollama.model`, `components.odoo.status`, `components.odoo.latencyMs`.
- ChatResponse enriquecido:
  - Estructura: `{ response: string, correlationId: string, intent?: string, entities?: Record<string, string> }`.

## 12. Calidad de Especificación

Documento atómico, testable y orientado a resultados, sin detalles de implementación más allá de compromisos del proyecto.
