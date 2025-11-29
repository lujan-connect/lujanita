@JIRA:LUJ-011
@P1
@FR-API-004
Feature: API de órdenes en el BFF
  Como cliente quiero consultar una orden por su ID

  Scenario: GET /api/orders/{id} retorna resumen de orden
    Given existe una orden en Odoo con id "SO001"
    When el BFF recibe GET /api/orders/SO001
    Then responde 200 con { orderId, status, totalAmount, customerName }

