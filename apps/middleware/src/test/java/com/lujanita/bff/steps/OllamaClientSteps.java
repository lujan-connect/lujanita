package com.lujanita.bff.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class OllamaClientSteps {

    @Given("Ollama está accesible y el modelo por defecto es {string}")
    public void ollama_accesible(String model) {
        // TODO: Configurar cliente/mock
        throw new io.cucumber.java.PendingException();
    }

    @When("el BFF envía mensajes {string} a ollama.chat")
    public void bff_envia_mensajes(String messagesJson) {
        // TODO: Invocar chat
        throw new io.cucumber.java.PendingException();
    }

    @Then("la respuesta incluye {string}")
    public void respuesta_incluye(String jsonSpec) {
        // TODO: Validar métricas y contenido
        throw new io.cucumber.java.PendingException();
    }

    @When("se configura {string}")
    public void configura_opciones(String optionsJson) {
        // TODO: Aplicar opciones
        throw new io.cucumber.java.PendingException();
    }

    @Given("el modelo configurado no existe")
    public void modelo_no_disponible() {
        // TODO: Forzar error MODEL_NOT_FOUND
        throw new io.cucumber.java.PendingException();
    }

    @Then("se retorna error LLM001 MODEL_NOT_FOUND y se registra alerta")
    public void valida_error_model_not_found() {
        // TODO: Verificar código y logging
        throw new io.cucumber.java.PendingException();
    }
}

