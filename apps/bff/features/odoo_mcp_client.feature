@JIRA:LUJ-014
@P1
@FR-MCP-001 @FR-MCP-005 @FR-MCP-007
Feature: Cliente MCP de Odoo en el BFF
  Como BFF necesito consultar datos de Odoo vía MCP

  Background:
    Given se propagan headers X-Api-Key, X-Role y X-Profile hacia MCP

  Scenario: orders.get sin líneas por defecto
    When el BFF invoca orders.get({ orderId: "SO001" })
    Then la respuesta incluye { orderId, status, customerName, totalAmount, createdAt } sin lines

  Scenario: orders.get con includeLines
    When el BFF invoca orders.get({ orderId: "SO001", includeLines: true })
    Then la respuesta incluye lines con { productId, productName, quantity, price }

  Scenario: customers.search con paginación por defecto
    When el BFF invoca customers.search({ query: "juan" })
    Then la respuesta incluye customers y totalCount con limit=20 offset=0 por defecto

