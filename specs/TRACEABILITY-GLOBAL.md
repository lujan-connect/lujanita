# Mapa de Trazabilidad Global (Specs 001-004)

## 001 Front UI
- Features: apps/widget/features/*
- Steps: apps/widget/steps/*
- FR cubiertos: FR-001, FR-003, FR-004, FR-005, FR-006, FR-007, FR-008, FR-010, FR-011, FR-012

## 002 BFF API REST
- Features: apps/middleware/features/bff_*.feature
- Steps: apps/middleware/src/test/java/com/lujanita/bff/steps/*
- FR cubiertos: FR-API-001..008 (validación headers, correlationId, chat, orders, health, errores, JSON, rate limit)

## 003 ollama-client
- Features: apps/middleware/features/ollama_client.feature
- Steps: apps/middleware/src/test/java/com/lujanita/bff/steps/OllamaClientSteps.java
- FR cubiertos: FR-LLM-001, FR-LLM-002, FR-LLM-006 (+ error LLM001)

## 004 odoo-mcp-client
- Features: apps/middleware/features/odoo_mcp_client.feature
- Steps: apps/middleware/src/test/java/com/lujanita/bff/steps/OdooMcpClientSteps.java
- FR cubiertos: FR-MCP-001, FR-MCP-005, FR-MCP-007 (+ paginación por defecto)

## Notas
- Este mapa se actualiza a medida que se agregan nuevos escenarios.
- Objetivo: asegurar cobertura BDD de los FR antes de implementar.

