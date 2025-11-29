@JIRA:LUJ-005
@P2
@FR-005 @FR-008
Feature: Accesibilidad e internacionalización
  Como usuario quiero una UI accesible y traducida

  Background:
    Given el sistema de i18n está cargado con claves camelCase y traducciones ES/EN

  Scenario: Navegación por teclado
    When el usuario navega con tab hasta el campo de entrada
    Then puede escribir y enviar el mensaje sin usar el mouse

  Scenario: Textos visibles provienen de traducciones
    When se abre el widget
    Then el título y el botón de enviar usan claves de i18n en español

