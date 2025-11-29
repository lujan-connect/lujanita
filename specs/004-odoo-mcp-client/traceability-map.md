# Mapa de Trazabilidad - 004 odoo-mcp-client

## Requisitos ↔ Features ↔ Steps

- FR-MCP-001 (Cliente MCP con orders.get, customers.search, products.list)
  - Feature: apps/middleware/features/odoo_mcp_client.feature
  - Steps: OdooMcpClientSteps.bff_orders_get, bff_customers_search (pendiente products.list si se agrega escenario)

- FR-MCP-005 (Propagar headers X-Api-Key, X-Role, X-Profile)
  - Feature: apps/middleware/features/odoo_mcp_client.feature (Background)
  - Steps: OdooMcpClientSteps.headers_mcp

- FR-MCP-007 (orders.get con includeLines opcional)
  - Feature: apps/middleware/features/odoo_mcp_client.feature (sin lines / con includeLines)
  - Steps: OdooMcpClientSteps.bff_orders_get, bff_orders_get_lines, respuesta_sin_lines, respuesta_con_lines

## Paginación y Defaults
- Por defecto limit=20, offset=0
  - Feature: apps/middleware/features/odoo_mcp_client.feature (customers.search por defecto)
  - Steps: OdooMcpClientSteps.bff_customers_search, respuesta_paginada_por_defecto

## Errores OD00X
- OD001 NOT_FOUND / OD002 VALIDATION_ERROR / OD003 UNAVAILABLE
  - Feature: agregar escenarios específicos si se requieren validar mapeos al BFF (MW00X)

## Cobertura y Notas
- Cobertura BDD inicial: orders.get (sin/with lines), customers.search por defecto; headers propagados
- Pendiente: agregar feature para `products.list` si se desea cubrir en v1

