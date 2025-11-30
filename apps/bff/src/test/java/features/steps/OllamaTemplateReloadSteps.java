package features.steps;

import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.cucumber.java.PendingException;

public class OllamaTemplateReloadSteps {
    @Dado("existe una plantilla para el role {string} y profile {string} versión {string}")
    public void existe_plantilla_role_profile_version(String role, String profile, String version) {
        throw new PendingException();
    }

    @Cuando("se modifica el archivo de plantilla y se incrementa el timestamp")
    public void modifica_archivo_plantilla_incrementa_timestamp() {
        throw new PendingException();
    }

    @Entonces("la siguiente generación usa la nueva versión de la plantilla")
    public void siguiente_generacion_usa_nueva_version() {
        throw new PendingException();
    }

    @Y("no se requiere reinicio del BFF")
    public void no_se_requiere_reinicio_bff() {
        throw new PendingException();
    }
}

