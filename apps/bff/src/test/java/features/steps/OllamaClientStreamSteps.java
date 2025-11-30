package features.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;

public class OllamaClientStreamSteps {
    @Dado("el modelo {string} está disponible en Ollama")
    public void modelo_disponible_en_ollama(String modelo) { throw new PendingException(); }

    @Dado("el prompt es válido para el role {string} y profile {string}")
    public void prompt_valido_role_profile(String role, String profile) { throw new PendingException(); }

    @Cuando("se envía una solicitud de generación con stream habilitado")
    public void envio_solicitud_stream() { throw new PendingException(); }

    @Entonces("se reciben fragmentos de respuesta en orden")
    public void recibe_fragmentos_en_orden() { throw new PendingException(); }

    @Y("el último fragmento tiene done=true y totalTokens > 0")
    public void ultimo_fragmento_done_totalTokens() { throw new PendingException(); }

    @Y("la latencia registrada es menor a 1500ms")
    public void latencia_menor_1500ms() { throw new PendingException(); }
}
