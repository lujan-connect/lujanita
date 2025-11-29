# data-model.md - 002-bff-api-rest

## Esquema de entidades BFF

- ChatRequest: { message, correlationId }
- ChatResponse: { response, correlationId }
- McpRequest: { method, params, headers }
- McpResponse: { ...dinámico según método MCP... }

## Relaciones
- Un ChatRequest produce un ChatResponse
- Un McpRequest produce un McpResponse

