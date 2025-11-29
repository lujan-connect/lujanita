@JIRA:LUJ-106
@P2
@FR-3008
Feature: Recarga dinámica de plantillas de prompt
  Como administrador quiero que al modificar una plantilla en disco, el cambio se refleje en la siguiente generación

  Scenario: Reload de plantilla tras modificación
    Given existe una plantilla para el role "guest" y profile "logistics" versión "v1"
    When se modifica el archivo de plantilla y se incrementa el timestamp
    Then la siguiente generación usa la nueva versión de la plantilla
    And no se requiere reinicio del BFF

