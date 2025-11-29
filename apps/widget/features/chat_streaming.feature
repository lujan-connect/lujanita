@JIRA:LUJ-006
@P1
@FR-012 @FR-013
Feature: Streaming de respuestas en la UI
  Como usuario quiero ver respuestas fluídas tipo chat mientras se generan

  Background:
    Given el widget está configurado con apiKey, role, profile y endpoint del BFF

  Scenario: Render incremental vía SSE
    When la UI inicia una conexión SSE con el BFF para la respuesta
    And el BFF emite eventos con fragmentos de texto
    Then el widget renderiza los fragmentos incrementales en la conversación
    And al finalizar, muestra la respuesta completa y marca el estado como hecho

  Scenario: Fallback a REST si SSE falla
    Given la conexión SSE no puede establecerse
    When la UI realiza un POST /api/chat
    Then el widget muestra la respuesta completa y registra el fallback en logs

