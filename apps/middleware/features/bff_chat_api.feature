@JIRA:LUJ-010
    Then responde 429 con código MW007 en las excedidas
    When el BFF procesa las solicitudes
    Given se realizan más de 60 solicitudes en un minuto con la misma apiKey
  Scenario: Rate limiting por apiKey

    Then responde 200 con { response: "...", correlationId }
    When el BFF recibe POST /api/chat con { message: "hola" }
  Scenario: POST /api/chat responde con texto y correlationId
  @smoke

    Given headers válidos con apiKey, role y profile
  Background:

  Como cliente (UI) quiero enviar mensajes al BFF y recibir una respuesta
Feature: API de chat en el BFF
@FR-API-001 @FR-API-002 @FR-API-003
@P1

