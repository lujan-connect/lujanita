package com.lujanita.bff.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class BffOrdersApiSteps {

    @Given("existe una orden en Odoo con id {string}")
    public void existe_orden(String id) {
        // TODO: Mock MCP respuesta orders.get
        throw new io.cucumber.java.PendingException();
    }

    @When("el BFF recibe GET /api/orders/{string}")
    public void bff_get_order(String id) {
        // TODO: Simular GET orders
        throw new io.cucumber.java.PendingException();
    }

    @Then("responde 200 con {string}")
    public void responde_200_con(String jsonSpec) {
        // TODO: Validar payload
        throw new io.cucumber.java.PendingException();
    }
}

