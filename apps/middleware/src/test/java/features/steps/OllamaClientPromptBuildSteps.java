package features.steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.cucumber.java.PendingException;

public class OllamaClientPromptBuildSteps {
    @Dado("existe una plantilla para el role {string} y profile {string}")
    public void existe_plantilla_role_profile(String role, String profile) { throw new PendingException(); }

    @Dado("el historial tiene {int} mensajes")
    public void historial_tiene_n_mensajes(int n) { throw new PendingException(); }

    @Cuando("se construye el prompt para el usuario")
    public void construye_prompt_usuario() { throw new PendingException(); }

    @Entonces("el prompt resultante concatena plantilla, overlay y mensaje")
    public void prompt_concatena_plantilla_overlay_mensaje() { throw new PendingException(); }

    @Y("si el prompt supera maxPromptChars, se trunca el historial y se emite un warning")
    public void prompt_truncado_warning() { throw new PendingException(); }

    @Y("el prompt es sanitizado antes de enviarse")
    public void prompt_sanitizado() { throw new PendingException(); }
}

