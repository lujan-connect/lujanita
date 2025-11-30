@JIRA:LUJ-013
@P1
@FR-LLM-001 @FR-LLM-002 @FR-LLM-006
Feature: Cliente Ollama en el BFF
  Como BFF necesito delegar tareas de lenguaje a Ollama

  Background:
    Given Ollama está accesible y el modelo por defecto es "tinyllama"

  Scenario: ollama.chat retorna respuesta y métricas mínimas
    When el BFF envía mensajes { role: 'user', content: 'Hola' } a ollama.chat
    Then la respuesta incluye { response, model, metrics: { durationMs, promptTokens?, completionTokens? } }

  Scenario: opciones v1 soportadas
    When se configura { temperature: 0.7, maxTokens: 128, stream: false }
    Then la inferencia se ejecuta con esas opciones

  Scenario: modelo no disponible
    Given el modelo configurado no existe
    When el BFF invoca ollama.chat
    Then se retorna error LLM001 MODEL_NOT_FOUND y se registra alerta

  @FR-LLM-008
  Scenario: Inclusión de systemPrompt y overrides por rol/perfil
    Given Ollama está accesible y el modelo por defecto es "tinyllama"
    And el cliente envía role "agent", profile "default", systemPrompt "Eres Lujanita, responde en español" y userPromptOverrides { "language": "es" }
    When el BFF envía mensajes a ollama.chat
    Then el primer mensaje es de role "system" con el contenido del systemPrompt
    And el mensaje de usuario aplica los overrides configurados

  @FR-LLM-009
  Scenario: Streaming de tokens
    Given Ollama soporta streaming
    When el BFF solicita respuesta con stream habilitado
    Then la respuesta se recibe en fragmentos ordenados y el último fragmento tiene done=true

  @FR-LLM-010
  Scenario: Fallback automático si falla el streaming
    Given Ollama soporta streaming
    And ocurre un error de red durante el stream
    When el BFF detecta la interrupción
    Then se realiza una solicitud de respuesta completa y se marca stream_fallback=true en logs

  @FR-LLM-011
  Scenario: Recarga dinámica de plantillas
    Given existe una plantilla de prompt para el rol "agent" y profile "default" versión "v1"
    When se modifica el archivo de plantilla y se incrementa el timestamp
    Then la siguiente generación usa la nueva versión de la plantilla sin reinicio del BFF

