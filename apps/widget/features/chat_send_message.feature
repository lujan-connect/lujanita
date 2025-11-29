@JIRA:LUJ-002
@P1
@FR-003 @FR-004 @FR-006
Feature: Envío de mensajes al BFF
  Como usuario quiero enviar un mensaje y recibir respuesta

  Background:
    Given el widget está configurado con apiKey, role, profile y endpoint del BFF

  @smoke
  Scenario: Enviar mensaje y recibir respuesta
    When el usuario escribe "¿Estado de mi pedido SO001?" y presiona enviar
    Then se realiza un POST /api/chat con headers apiKey, role, profile y correlationId
    And la UI muestra la respuesta del BFF

  Scenario: Generación y trazabilidad de correlationId
    When se envía un mensaje desde el widget
    Then se genera un correlationId y se registra en consola

