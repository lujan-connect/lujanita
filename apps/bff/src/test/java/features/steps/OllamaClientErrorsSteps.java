package features.steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.PendingException;

public class OllamaClientErrorsSteps {
    @Dado("el modelo {string} está disponible")
    public void modelo_disponible(String modelo) { throw new PendingException(); }

    @Dado("el modelo {string} no está disponible")
    public void modelo_no_disponible(String modelo) { throw new PendingException(); }

    @Cuando("se simula un timeout en la respuesta de Ollama")
    public void simula_timeout_ollama() { throw new PendingException(); }

    @Cuando("se solicita generación con ese modelo")
    public void solicita_generacion_modelo() { throw new PendingException(); }

    @Cuando("se interrumpe el stream de tokens")
    public void interrumpe_stream_tokens() { throw new PendingException(); }

    @Entonces("se recibe un error con código LLM001")
    public void error_llm001() { throw new PendingException(); }

    @Entonces("se recibe un error con código LLM002")
    public void error_llm002() { throw new PendingException(); }

    @Entonces("se recibe un error con código LLM003")
    public void error_llm003() { throw new PendingException(); }
}

