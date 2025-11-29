# Mapa de Trazabilidad - 002 BFF API REST
- Pendiente: validaciones de autorización por endpoint (role/profile) → agregar escenarios específicos si se requiere
- Steps: esqueleto creado en `src/test/java/com/lujanita/bff/steps/*`
- Cobertura BDD inicial: completa para FR-API-001..008
## Cobertura y Notas

  - Steps: BffChatApiSteps.muchas_solicitudes, BffChatApiSteps.procesa_solicitudes, BffChatApiSteps.responde_429
  - Feature: apps/middleware/features/bff_chat_api.feature (Rate limiting)
- FR-API-008 (Rate limiting 60/min)

  - Steps: Validar payloads en Then `responde_200_con`
  - Feature: apps/middleware/features/bff_chat_api.feature / bff_orders_api.feature / bff_health_api.feature
- FR-API-007 (JSON camelCase)

  - Steps: BffChatApiSteps.responde_429
  - Feature: apps/middleware/features/bff_chat_api.feature (Rate limiting, errores generales)
- FR-API-006 (Errores MW00X)

  - Steps: BffHealthApiSteps.bff_get_health, BffHealthApiSteps.responde_200_con
  - Feature: apps/middleware/features/bff_health_api.feature
- FR-API-005 (GET /health con componentes)

  - Steps: BffOrdersApiSteps.existe_orden, BffOrdersApiSteps.bff_get_order, BffOrdersApiSteps.responde_200_con
  - Feature: apps/middleware/features/bff_orders_api.feature
- FR-API-004 (GET /api/orders/{id})

  - Steps: BffChatApiSteps.bff_post_api_chat, BffChatApiSteps.responde_200_con
  - Feature: apps/middleware/features/bff_chat_api.feature
- FR-API-003 (POST /api/chat con intent/entities opcional)

  - Steps: BffChatApiSteps.bff_post_api_chat → respuesta con correlationId
  - Feature: apps/middleware/features/bff_chat_api.feature (Chat)
- FR-API-002 (CorrelationId)

  - Steps: BffChatApiSteps.headers_validos
  - Feature: apps/middleware/features/bff_chat_api.feature (Background)
- FR-API-001 (Validar headers apiKey/role/profile)

## Requisitos ↔ Features ↔ Steps


