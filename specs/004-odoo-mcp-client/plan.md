---
title: 004 - Plan de Implementación odoo-mcp-client
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [mcp, odoo, client, bff, plan]
---

# Plan de Implementación - 004 odoo-mcp-client

## Resumen
Cliente del BFF para interactuar con el MCP Server de Odoo. Provee operaciones de consulta: `orders.get`, `customers.search`, `products.list`, con validación de parámetros, paginación por defecto y mapeo de errores.

---

## Arquitectura y Responsabilidad
- Ubicación: `apps/middleware/src/main/java/com/lujanita/bff/client/mcp/OdooMcpClient.java`
- Responsabilidad: encapsular invocaciones MCP y transformar respuestas a DTOs del BFF.
- Integración: usado por `OrdersService` y otras capas del BFF.

---

## Autenticación y Headers
- Propagar desde BFF:
  - `X-Api-Key`: api key del cliente
  - `X-Role`: rol del cliente
  - `X-Profile`: perfil operativo
- Si MCP requiere credenciales propias:
  - `Authorization: Bearer <MCP_TOKEN>` o par `client_id/client_secret` según configuración
- `correlationId`: incluir en headers/contexto para trazabilidad

---

## Operaciones y Flujos

### orders.get(orderId, includeLines=false)
- Input: `{ orderId: string, includeLines?: boolean }`
- Output: `{ orderId, status, customerName, totalAmount, createdAt, lines? }`
- Flujo:
  1) Validar `orderId`
  2) Construir request MCP y enviar con headers propagados
  3) Si `includeLines=true` → incluir `lines` en respuesta
  4) Mapear errores a `OD00X` y retornar al BFF

### customers.search(query, limit=20, offset=0)
- Input: `{ query: string, limit?: number, offset?: number }`
- Output: `{ customers: Customer[], totalCount: number }`
- Flujo:
  1) Validar parámetros y asignar por defecto `limit=20`, `offset=0`
  2) Enviar búsqueda a MCP
  3) Retornar clientes y `totalCount`

### products.list(filters?, limit=20, offset=0)
- Input: `{ available?: boolean, limit?: number, offset?: number }`
- Output: `{ products: Product[], totalCount: number }`
- Flujo similar a `customers.search`

---

## DTOs (borrador)
- `OrdersGetRequest(orderId, includeLines?)`
- `OrdersGetResponse(orderId, status, customerName, totalAmount, createdAt, lines?)`
- `OrderLine(productId, productName, quantity, price)`
- `CustomersSearchRequest(query, limit?, offset?)`
- `CustomersSearchResponse(customers[], totalCount)`
- `ProductsListRequest(available?, limit?, offset?)`
- `ProductsListResponse(products[], totalCount)`

---

## Paginación y Defaults
- Por defecto: `limit=20`, `offset=0` si no se especifica
- Incluir `totalCount` en respuestas de listados

---

## Errores y Códigos (OD00X)
- `OD001 NOT_FOUND`: recurso no encontrado
- `OD002 VALIDATION_ERROR`: parámetros inválidos
- `OD003 UNAVAILABLE`: servicio MCP no disponible
- Mapeo al BFF: `MW00X` con detalle del origen

---

## Configuración
- `ODOO_MCP_URL=http://localhost:8069/mcp`
- `MCP_TIMEOUT=15000` ms
- Credenciales: `MCP_TOKEN` (si aplica)

---

## Testing (Test-First)
- BDD: `apps/middleware/features/odoo_mcp_client.feature`
  - orders.get sin líneas por defecto y con includeLines
  - customers.search con paginación por defecto
- Unit: validar parámetros y mapeo de respuestas
- Integración: simular MCP con WireMock

Secuencia:
1) Correr BDD (rojo)
2) Implementar `OdooMcpClient` mínimo
3) Correr BDD (verde)
4) Añadir unit tests para errores `OD00X`

---

## Riesgos & Mitigaciones
- Cambios en contratos MCP → mantener mocks y versionado
- Timeouts → usar reintentos con backoff limitado
- Credenciales → rotación y auditoría (no loguear tokens)

---

## Convenciones MCP de Odoo (llm_mcp_server)

- Terminología: MCP = Model Context Protocol (no “Model Content”).
- Endpoint base (ejemplo): `${ODOO_MCP_URL}` (configurable en `application.yml`).
- Autenticación:
  - Propagar `X-Api-Key`, `X-Role`, `X-Profile` desde el BFF.
  - Usar `Authorization: Bearer <MCP_TOKEN>` si el servidor MCP de Odoo está configurado con token.
- Operaciones típicas expuestas (nomenclatura indicativa):
  - `orders.get` → entrada `{ orderId, includeLines? }`, salida `{ orderId, status, customerName, totalAmount, createdAt, lines? }`
  - `customers.search` → entrada `{ query, limit?, offset? }`, salida `{ customers[], totalCount }`
  - `products.list` → entrada `{ available?, limit?, offset? }`, salida `{ products[], totalCount }`
- Formato de payloads:
  - JSON con claves camelCase en el BFF.
  - Alinear con contratos publicados en `packages/contracts` y mocks.
- Paginación por defecto:
  - `limit=20`, `offset=0` si no se especifica; incluir `totalCount` en respuestas.
- Errores MCP (OD00X) y mapeo:
  - `OD001 NOT_FOUND`, `OD002 VALIDATION_ERROR`, `OD003 UNAVAILABLE` → mapear a `MW00X` con `correlationId` y causa.
- Observabilidad:
  - Incluir `correlationId` en todas las llamadas; loguear `mcpOperation`, latencia y resultado.
