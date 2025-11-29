package com.lujanita.bff.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class OdooMcpClientSteps {

    @Given("se propagan headers X-Api-Key, X-Role y X-Profile hacia MCP")
    public void headers_mcp() {
        // TODO: Preparar propagación de headers
        throw new io.cucumber.java.PendingException();
    }

    @When("el BFF invoca orders.get({string})")
    public void bff_orders_get(String requestJson) {
        // TODO: Llamar cliente MCP
        throw new io.cucumber.java.PendingException();
    }

    @Then("la respuesta incluye {string} sin lines")
    public void respuesta_sin_lines(String jsonSpec) {
        // TODO: Validar ausencia de lines
        throw new io.cucumber.java.PendingException();
    }

    @When("el BFF invoca orders.get({string}, includeLines: true)")
    public void bff_orders_get_lines(String requestJson) {
        // TODO: Llamar con includeLines
        throw new io.cucumber.java.PendingException();
    }

    @Then("la respuesta incluye lines con {string}")
    public void respuesta_con_lines(String jsonSpec) {
        // TODO: Validar contenido de lines
        throw new io.cucumber.java.PendingException();
    }

    @When("el BFF invoca customers.search({string})")
    public void bff_customers_search(String requestJson) {
        // TODO: Llamar búsqueda
        throw new io.cucumber.java.PendingException();
    }

    @Then("la respuesta incluye customers y totalCount con limit=20 offset=0 por defecto")
    public void respuesta_paginada_por_defecto() {
        // TODO: Validar paginación por defecto
        throw new io.cucumber.java.PendingException();
    }
}

