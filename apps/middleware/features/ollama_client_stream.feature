@JIRA:LUJ-101
    And la latencia registrada es menor a 1500ms
    And el último fragmento tiene done=true y totalTokens > 0
    Then se reciben fragmentos de respuesta en orden
    When se envía una solicitud de generación con stream habilitado
    And el prompt es válido para el role "guest" y profile "logistics"
    Given el modelo "llama3:8b-instruct" está disponible en Ollama
  Scenario: Respuesta streaming exitosa

  Como servicio BFF quiero recibir la respuesta de Ollama en modo streaming para reenviarla al frontend token a token
Feature: Streaming de respuestas desde Ollama
@FR-3002 @FR-3003
@P1

