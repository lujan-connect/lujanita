@JIRA:LUJ-007 @P1 @FR-004 @FR-006
Feature: Integración real con el BFF al enviar mensajes
  Como usuario quiero que el widget llame al BFF y muestre la respuesta del asistente con correlationId

  Background:
    Given el widget está configurado con apiKey "demo-key", role "guest", profile "default" y endpoint "http://bff.test"

  Scenario: Enviar mensaje y mostrar respuesta del asistente
    When el usuario escribe "¿Estado de mi pedido?" y presiona enviar en el widget React
    Then se hace una llamada POST /api/chat con headers correctos
    And la UI muestra la respuesta del asistente con correlationId distinto al del usuario
