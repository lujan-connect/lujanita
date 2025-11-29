@JIRA:LUJ-102
@P1
@FR-3004
Feature: Fallback automático a respuesta completa si falla el streaming
  Como servicio BFF quiero que si el streaming falla, se realice automáticamente una solicitud de respuesta completa

  Scenario: Fallback tras timeout de streaming
    Given el modelo "llama3:8b-instruct" está disponible en Ollama
    And el prompt es válido para el role "guest" y profile "logistics"
    When se envía una solicitud de generación con stream habilitado y ocurre un timeout
    Then se realiza una solicitud de respuesta completa
    And se recibe la respuesta final con totalTokens > 0
    And se registra stream_fallback=true en logs

