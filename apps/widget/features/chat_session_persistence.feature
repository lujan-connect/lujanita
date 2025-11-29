@JIRA:LUJ-004
@P2
@FR-011
Feature: Persistencia de sesión de conversación
  Como usuario quiero que la conversación se mantenga al recargar la página

  Background:
    Given `sessionStorage` está disponible y el widget usa la clave "lujanita:session:<siteId>"

  Scenario: Mensajes sobreviven recarga
    Given el usuario ha enviado 2 mensajes
    And se guardan en sessionStorage
    When el usuario recarga la página
    Then el widget restaura la conversación desde sessionStorage

