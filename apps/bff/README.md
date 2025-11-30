# Lujanita BFF - Pruebas BDD

## Ejecutar pruebas BDD (deben fallar inicialmente)

```bash
cd apps/middleware
mvn test -Dcucumber.filter.tags="@bdd"  # Ajusta si tienes runners configurados
```

## Features
- features/bff_chat_api.feature
- features/bff_orders_api.feature
- features/bff_health_api.feature
- features/ollama_client.feature
- features/odoo_mcp_client.feature

## Implementar Steps
- Ubicación: `apps/middleware/src/test/java/com/lujanita/bff/steps/*`
- Usa Cucumber + JUnit + RestAssured/WireMock/MCP client según escenario

## Notas
- Sigue SDD: tests primero (rojo), implementación mínima, verde
- Observabilidad: correlationId, códigos MW00X/LLM00X/OD00X, logs JSON
```
