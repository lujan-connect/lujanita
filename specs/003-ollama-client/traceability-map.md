# Mapa de Trazabilidad - 003 ollama-client

## Requisitos ↔ Features ↔ Steps

- FR-LLM-001 (DTOs chat con messages/options)
  - Feature: apps/middleware/features/ollama_client.feature
  - Steps: OllamaClientSteps.bff_envia_mensajes, respuesta_incluye

- FR-LLM-002 (Configurar model y opciones v1)
  - Feature: apps/middleware/features/ollama_client.feature (opciones soportadas)
  - Steps: OllamaClientSteps.configura_opciones

- FR-LLM-006 (Métricas: durationMs obligatorio; tokens cuando disponibles)
  - Feature: apps/middleware/features/ollama_client.feature (respuesta y métricas)
  - Steps: OllamaClientSteps.respuesta_incluye

- FR-LLM-008 (systemPrompt y userPromptOverrides por rol/perfil)
  - Feature: apps/middleware/features/ollama_client.feature (escenario adicional)
  - Steps: (Por implementar) Validar que el primer mensaje es `system` con systemPrompt y que el de `user` aplica overrides

## Errores LLM00X
- LLM001 MODEL_NOT_FOUND
  - Feature: apps/middleware/features/ollama_client.feature (modelo no disponible)
  - Steps: OllamaClientSteps.modelo_no_disponible, valida_error_model_not_found

## Cobertura y Notas
- Cobertura BDD inicial: FR-LLM-001/002/006/008 + error LLM001
- Pendiente: escenario para invalid options (LLM003) si se decide testear v1 estricta
