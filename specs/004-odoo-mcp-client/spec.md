---
title: odoo-mcp-client
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [middleware, mcp, odoo, contracts, sdd]
spec_phase: specify
---

# 004 - odoo-mcp-client

## Permalink: odoo-mcp-client

Diseñar la interacción del middleware con el MCP Server de Odoo para consultar órdenes, clientes y productos.

## 1. Propósito y Alcance

**Propósito**: Proveer un cliente MCP robusto para invocar operaciones de Odoo desde el middleware con contratos y errores claros.

**Alcance**:
- In scope:
  - Operación `orders.get`: obtener detalles mínimos de una orden
  - Operación `customers.search`: búsqueda básica por query
  - Operación `products.list`: lista de productos con filtros básicos
  - Mapeo de errores MCP a `OD00X`
- Out of scope:
  - Mutaciones complejas (crear/actualizar)
- Future scope:
  - Paginación, filtros avanzados y mutaciones

## 2. Definiciones

- MCP: Model Context Protocol
- Modelos Odoo: `sale.order`, `res.partner`, `product.product`

## 3. Historias de Usuario y Criterios

### US-MCP-001: Obtener orden
Como middleware necesito consultar una orden por ID.
- Given existe `SO001` en Odoo
- When `orders.get({ orderId: 'SO001' })`
- Then retorna `orderId, status, customerName, totalAmount, createdAt`

### US-MCP-002: Buscar clientes
Como middleware necesito una búsqueda simple de clientes.
- Given query "juan"
- When `customers.search({ query: 'juan', limit: 10 })`
- Then retorna lista de clientes con `customerId, name, email, phone?`

### US-MCP-003: Listar productos
Como middleware necesito listar productos disponibles.
- Given `available=true`
- When `products.list({ available: true, limit: 20 })`
- Then retorna `products[]` y `totalCount`

## 4. Requisitos Funcionales

- FR-MCP-001: Implementar cliente MCP con operaciones `orders.get`, `customers.search`, `products.list`.
- FR-MCP-002: Validar parámetros y mapear errores MCP a `OD00X`.
- FR-MCP-003: Logs estructurados con `correlationId` y `mcpOperation`.
- FR-MCP-004: Tiempos de espera y reintentos configurables.
- FR-MCP-005: Propagar headers `X-Api-Key`, `X-Role`, `X-Profile` hacia MCP.
- FR-MCP-006: Paginación por defecto (`limit=20`, `offset=0`) y `totalCount` en respuestas cuando aplique.
- FR-MCP-007: `orders.get` incluye `lines` sólo si `includeLines=true`.

## 5. Criterios de Éxito

- SC-MCP-001: 95% de respuestas en < 1.5s (consulta simple).
- SC-MCP-002: Errores MCP mapeados correctamente.
- SC-MCP-003: Contratos alineados con `.github/copilot-knowledge/contracts-mcp.md`.

## 6. Entidades

- OrdersGetRequest, OrdersGetResponse, OrderLine
- CustomersSearchRequest, CustomersSearchResponse, Customer
- ProductsListRequest, ProductsListResponse, Product

## 7. Supuestos

- MCP server accesible y autenticado
- Contratos disponibles en `packages/contracts`

## 8. Dependencias

- `.github/copilot-knowledge/contracts-mcp.md`
- `packages/contracts/mocks/`

## 9. Riesgos

- Campos faltantes o cambios de contrato: versionado y mocks
- Timeouts: usar backoff y reintentos

## 10. Aceptación (Resumen)

- `orders.get` → respuesta con campos mínimos
- `customers.search` → lista con conteo
- `products.list` → productos y conteo

## 11. [NEEDS CLARIFICATION]

1) Autenticación MCP:
   - [RESUELTO] Se usará autenticación por token del middleware propagado al MCP (header `X-Api-Key`) sin token adicional en v1. El `role` y `profile` se incluyen como metadatos (`X-Role`, `X-Profile`).
2) Límites y paginación:
   - [RESUELTO] Paginación condicional: si `limit` no se especifica, por defecto `limit=20` y `offset=0`. Respuestas incluirán `totalCount` cuando aplique.
3) Campos opcionales:
   - [RESUELTO] En `orders.get` las `lines` son opcionales y deshabilitadas por defecto; se incluyen sólo si `includeLines=true`.

## 12. Decisiones de Clarificación

- Autenticación MCP v1:
  - Headers: `X-Api-Key`, `X-Role`, `X-Profile` propagados desde el frontend→middleware→MCP.
  - No se requiere token adicional específico de Odoo MCP en v1.
- Paginación:
  - Parámetros: `limit` (por defecto 20), `offset` (por defecto 0).
  - Respuesta: `totalCount` incluido para `products.list` y `customers.search`.
- Detalle de órdenes:
  - `orders.get`: responde sin `lines` por defecto; `lines` se incluyen si `includeLines=true`.

## 13. Calidad de Especificación

Especificación atómica y alineada con contratos MCP.
