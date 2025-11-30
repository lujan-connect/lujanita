package com.lujanita.bff.steps;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class BffHealthApiSteps {

    @When("el BFF recibe GET /health")
    public void bff_get_health() {
        // TODO: Simular GET health
        throw new io.cucumber.java.PendingException();
    }

    @Then("responde 200 con {string}")
    public void responde_200_con(String jsonSpec) {
        // TODO: Validar componentes y versión
        throw new io.cucumber.java.PendingException();
    }
}

