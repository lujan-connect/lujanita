@JIRA:LUJ-103
@P1
@FR-3001 @FR-3009 @FR-3012
Feature: Construcción y validación de prompt para Ollama
  Como servicio BFF quiero construir el prompt combinando plantilla, overlay de role/profile y mensaje del usuario

  Scenario: Prompt válido y truncado si excede límite
    Given existe una plantilla para el role "guest" y profile "logistics"
    And el historial tiene 10 mensajes
    When se construye el prompt para el usuario
    Then el prompt resultante concatena plantilla, overlay y mensaje
    And si el prompt supera maxPromptChars, se trunca el historial y se emite un warning
    And el prompt es sanitizado antes de enviarse

