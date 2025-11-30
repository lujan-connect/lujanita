package features.steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.cucumber.java.PendingException;

public class OllamaClientMetricsSteps {
    @Dado("el modelo {string} está disponible")
    public void modelo_disponible(String modelo) { throw new PendingException(); }

    @Cuando("se realiza una generación de respuesta")
    public void realiza_generacion_respuesta() { throw new PendingException(); }

    @Entonces("se registra la métrica ollama_latency_ms")
    public void registra_metric_latency() { throw new PendingException(); }

    @Y("se registra ollama_tokens_in y ollama_tokens_out")
    public void registra_tokens_in_out() { throw new PendingException(); }

    @Y("los logs contienen correlationId, llmModel, role y profile")
    public void logs_contienen_campos() { throw new PendingException(); }
}

