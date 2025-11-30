@JIRA:LUJ-105
@P1
@FR-3007
Feature: Manejo de errores y códigos LLM00X en cliente Ollama
  Como desarrollador quiero recibir códigos de error claros cuando Ollama falla

  Scenario: Timeout mapeado a LLM001
    Given el modelo "llama3:8b-instruct" está disponible
    When se simula un timeout en la respuesta de Ollama
    Then se recibe un error con código LLM001

  Scenario: Modelo inexistente mapeado a LLM002
    Given el modelo "fantasy-llama" no está disponible
    When se solicita generación con ese modelo
    Then se recibe un error con código LLM002

  Scenario: Stream interrumpido mapeado a LLM003
    Given el modelo "llama3:8b-instruct" está disponible
    When se interrumpe el stream de tokens
    Then se recibe un error con código LLM003

