@JIRA:LUJ-012
@P1
@FR-API-005
Feature: Health del BFF
  Como operador quiero verificar el estado del servicio

  Scenario: GET /health retorna estado y componentes
    When el BFF recibe GET /health
    Then responde 200 con { status, version, components: { ollama: { status, model }, odoo: { status, latencyMs } } }

