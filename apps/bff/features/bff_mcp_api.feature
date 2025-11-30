@JIRA:LUJ-015
@P1
@FR-API-004
Feature: API genérica MCP en el BFF
  Como cliente quiero invocar cualquier método MCP vía un endpoint genérico

  Background:
    Given headers válidos con apiKey, role y profile

  Scenario: POST /api/mcp/orders.get retorna resumen de orden
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
      | orderId | SO001 |
    Then la respuesta MCP incluye { orderId, status, totalAmount, customerName }

  Scenario: POST /api/mcp/customers.search retorna lista de clientes
    When el cliente invoca el endpoint MCP genérico "customers.search" con parámetros
      | query | juan |
    Then la respuesta MCP incluye { customers, totalCount }

  Scenario: POST /api/mcp/products.search retorna lista de productos
    When el cliente invoca el endpoint MCP genérico "products.search" con parámetros
      | query | demo |
    Then la respuesta MCP incluye { products, totalCount }

  Scenario: POST /api/mcp/orders.list retorna lista de órdenes
    When el cliente invoca el endpoint MCP genérico "orders.list" con parámetros
      | limit | 10 |
    Then la respuesta MCP incluye { orders, totalCount }

  Scenario: POST /api/mcp/unknown.method retorna error simulado
    When el cliente invoca el endpoint MCP genérico "unknown.method" con parámetros
    Then la respuesta MCP incluye { ok, message }

  Scenario: POST /api/mcp/orders.get sin apiKey retorna error 401
    When el cliente invoca el endpoint MCP genérico "orders.get" sin apiKey
    Then la respuesta MCP es 401 MW001

  Scenario: POST /api/mcp/orders.get con parámetros vacíos retorna error de validación
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
    Then la respuesta MCP incluye { code, message }

  Scenario: POST /api/mcp/orders.get con apiKey inválida retorna error de autenticación
    Given headers inválidos sin apiKey
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
      | orderId | SO001 |
    Then la respuesta MCP es 401 MW001

  Scenario: POST /api/mcp/orders.get con backend MCP caído retorna error 502
    Given el backend MCP no responde
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
      | orderId | SO001 |
    Then la respuesta MCP incluye { code, correlationId }
    And el código es MW005

  Scenario: POST /api/mcp/orders.get con parámetros extra
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
      | orderId | SO001 |
      | extra   | valor |
    Then la respuesta MCP incluye { orderId, status, totalAmount, customerName }

  Scenario: POST /api/mcp/orders.get con tipo de dato incorrecto
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
      | orderId | 12345 |
    Then la respuesta MCP incluye { code, message }

  Scenario: POST /api/mcp/orders.get con headers adicionales
    Given headers válidos con apiKey, role, profile y un header extra
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
      | orderId | SO001 |
    Then la respuesta MCP incluye { orderId, status, totalAmount, customerName }

  Scenario: POST /api/mcp/orders.get con profile diferente
    Given headers válidos con apiKey, role y profile "admin"
    When el cliente invoca el endpoint MCP genérico "orders.get" con parámetros
      | orderId | SO001 |
    Then la respuesta MCP incluye { orderId, status, totalAmount, customerName }
