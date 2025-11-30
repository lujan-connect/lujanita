# Mapa de Trazabilidad Global (Specs 001-004)

## 001 Front UI
- Features: apps/widget/features/*
- Steps: apps/widget/steps/*
- FR cubiertos: FR-001, FR-003, FR-004, FR-005, FR-006, FR-007, FR-008, FR-010, FR-011, FR-012

## 002 BFF API REST
- Features: apps/middleware/features/bff_chat_api.feature, apps/middleware/features/bff_health_api.feature, apps/middleware/features/bff_mcp_api.feature
- Steps: apps/middleware/src/test/java/com/lujanita/bff/steps/BffChatApiSteps.java, apps/middleware/src/test/java/com/lujanita/bff/steps/OdooMcpClientSteps.java
- FR cubiertos: FR-API-001..008 (validación headers, correlationId, chat, health, errores, JSON, rate limit, endpoint MCP genérico)
- Notas: El endpoint de órdenes fue reemplazado por un endpoint genérico para MCP (`/api/mcp/{method}`), permitiendo invocar cualquier método/tool MCP (orders.get, customers.search, etc.)

## 003 ollama-client
- Features: apps/middleware/features/ollama_client.feature, apps/middleware/features/ollama_client_*.feature
- Steps: apps/middleware/src/test/java/com/lujanita/bff/steps/OllamaClientSteps.java
- FR cubiertos: FR-LLM-001, FR-LLM-002, FR-LLM-006 (+ error LLM001)

## 004 odoo-mcp-client
- Features: apps/middleware/features/odoo_mcp_client.feature, apps/middleware/features/bff_mcp_api.feature
- Steps: apps/middleware/src/test/java/com/lujanita/bff/steps/OdooMcpClientSteps.java
- FR cubiertos: FR-MCP-001, FR-MCP-005, FR-MCP-007 (+ paginación por defecto, validación de parámetros, errores MW004/MW005)
- Notas: El mock MCP simula el protocolo Model Content Protocol y la ejecución de tools, cubriendo edge cases y errores.

## Notas
- Este mapa se actualiza a medida que se agregan nuevos escenarios.
- Objetivo: asegurar cobertura BDD de los FR antes de implementar.
