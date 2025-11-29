@JIRA:LUJ-003
@P1
@FR-007
Feature: Manejo de errores de BFF en la UI
  Como usuario quiero ver mensajes claros cuando ocurren errores

  Background:
    Given el widget está configurado con apiKey, role, profile y endpoint del BFF

  Scenario: Error 403 por rol inválido
    Given el BFF responde 403 para la solicitud actual
    When el usuario envía un mensaje
    Then la UI muestra un error traducido con código UI003
    And se ofrece opción de reintento

  Scenario: Error de timeout
    Given la llamada al BFF excede el tiempo de espera
    When el usuario envía un mensaje
    Then la UI muestra un error con código UI006

