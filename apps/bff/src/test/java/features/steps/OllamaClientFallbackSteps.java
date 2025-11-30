package features.steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.cucumber.java.PendingException;

public class OllamaClientFallbackSteps {
    @Dado("el modelo {string} está disponible en Ollama")
    public void modelo_disponible_en_ollama(String modelo) { throw new PendingException(); }

    @Dado("el prompt es válido para el role {string} y profile {string}")
    public void prompt_valido_role_profile(String role, String profile) { throw new PendingException(); }

    @Cuando("se envía una solicitud de generación con stream habilitado y ocurre un timeout")
    public void envio_stream_timeout() { throw new PendingException(); }

    @Entonces("se realiza una solicitud de respuesta completa")
    public void realiza_fallback_completo() { throw new PendingException(); }

    @Y("se recibe la respuesta final con totalTokens > 0")
    public void recibe_respuesta_final() { throw new PendingException(); }

    @Y("se registra stream_fallback=true en logs")
    public void registra_stream_fallback_log() { throw new PendingException(); }
}

