# data-model.md - 004-odoo-mcp-client

## Esquema de entidades MCP

- McpRequest: { method, params, headers }
- McpResponse: { ...dinámico según método... }
- Order: { orderId, status, totalAmount, customerName, lines }
- Customer: { customerId, customerName, email, phone }
- Product: { productId, productName, price }

## Relaciones
- Un McpRequest produce un McpResponse
- Un Order puede tener múltiples lines

## Ejemplo de McpRequest (orders.get)
```json
{
  "method": "orders.get",
  "params": { "orderId": "SO001" },
  "headers": {
    "X-Api-Key": "...",
    "X-Role": "user",
    "X-Profile": "default"
  }
}
```

## Ejemplo de McpResponse (orders.get)
```json
{
  "orderId": "SO001",
  "status": "confirmed",
  "customerName": "Juan Perez",
  "totalAmount": 123.45,
  "createdAt": "2025-11-29T10:00:00Z",
  "lines": [
    { "productId": "P001", "productName": "Producto 1", "quantity": 2, "price": 50.0 }
  ]
}
```

## Ejemplo de error estructurado (MW004/MW005)
```json
{
  "code": "MW004",
  "message": "Parámetros inválidos"
}
```

## Notas sobre extensibilidad
- El campo `method` permite invocar cualquier tool soportado por el MCP server de Odoo (orders.get, customers.search, products.search, etc).
- Los headers son obligatorios para autenticación y autorización.
- La estructura de `params` depende del método/tool invocado.
- Los errores siguen el esquema `{ code, message, correlationId? }`.

## Referencia
- Protocolo MCP: [Odoo llm_mcp_server](https://apps.odoo.com/apps/modules/18.0/llm_mcp_server)
- Documentación interna: `/docs/odoo-mcp.md`
