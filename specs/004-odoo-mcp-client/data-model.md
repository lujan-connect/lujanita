---
title: 004 - Data Model (odoo-mcp-client)
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [mcp, odoo, data-model]
---

# Modelos de Datos - odoo-mcp-client

Alineado con MCP (Model Context Protocol) y el servidor de Odoo (`llm_mcp_server`). Claves en camelCase para coherencia con el BFF.

## Orders

### OrdersGetRequest
- orderId: string (obligatorio)
- includeLines: boolean (opcional, por defecto false)

### OrdersGetResponse
- orderId: string
- status: string
- customerName: string
- totalAmount: number
- createdAt: string (ISO 8601)
- lines?: OrderLine[]

### OrderLine
- productId: string
- productName: string
- quantity: number
- price: number

## Customers

### CustomersSearchRequest
- query: string (obligatorio)
- limit: number (opcional, por defecto 20)
- offset: number (opcional, por defecto 0)

### CustomersSearchResponse
- customers: Customer[]
- totalCount: number

### Customer
- customerId: string
- name: string
- email: string
- phone?: string

## Products

### ProductsListRequest
- available: boolean (opcional)
- limit: number (opcional, por defecto 20)
- offset: number (opcional, por defecto 0)

### ProductsListResponse
- products: Product[]
- totalCount: number

### Product
- productId: string
- name: string
- price: number
- available: boolean

## Errores MCP (OD00X)
- code: string (OD001, OD002, OD003)
- message: string
- details?: any

## Notas
- `totalCount` debe incluirse en listados para soportar paginación.
- `createdAt` en formato ISO 8601.
- `lines` en `OrdersGetResponse` sólo cuando `includeLines=true`.

