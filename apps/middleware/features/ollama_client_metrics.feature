@JIRA:LUJ-104
@P1
@FR-3005 @FR-3006
Feature: Registro de métricas y logs en generación Ollama
  Como ingeniero de observabilidad quiero que se registren métricas de latencia, tokens y logs con correlationId

  Scenario: Métricas y logs correctos en generación exitosa
    Given el modelo "llama3:8b-instruct" está disponible
    When se realiza una generación de respuesta
    Then se registra la métrica ollama_latency_ms
    And se registra ollama_tokens_in y ollama_tokens_out
    And los logs contienen correlationId, llmModel, role y profile

