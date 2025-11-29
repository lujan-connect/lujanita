@JIRA:LUJ-001 @P1 @FR-001 @FR-010 @FR-012
Feature: Embedding del widget Lujanita en páginas landing
  Como visitante quiero abrir el widget y poder interactuar

  Background:
    Given existe un contenedor con id "lujanita-widget" en la página
    And se configura el widget con apiKey "demo-key", role "guest", profile "default" y endpoint del BFF

  @smoke
  Scenario: Render del widget en el contenedor designado
    When se inicializa LujanitaWidget con la configuración
    Then el componente se renderiza dentro del contenedor
    And los controles del chat son visibles y accesibles

  Scenario: Theming básico aplicado
    When se inicializa LujanitaWidget con theme "{ primaryColor: '#0066cc', backgroundColor: '#ffffff' }"
    Then el botón de apertura usa el color primario
    And el panel del chat muestra el color de fondo configurado
