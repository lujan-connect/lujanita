---
- Cambios en API Ollama → envolver en capa de compatibilidad
- Faltan métricas → registrar `metricsMissing=true` y continuar
- Latencia elevada en CPU → limitar `maxTokens`, ajustar `temperature`
## Riesgos & Mitigaciones

---

4) Añadir unit tests para errores y límites de opciones
3) Correr BDD (verde)
2) Implementar `OllamaClient` mínimo
1) Correr BDD (rojo)
Secuencia:

- Integración: simular endpoint Ollama (WireMock o SDK en modo mock)
- Unit: pruebas de normalización de métricas y validación de opciones
  - Error modelo no disponible
  - Opciones soportadas v1
  - Respuesta con métricas mínimas
- BDD: `apps/middleware/features/ollama_client.feature`
## Testing (Test-First)

---

- Opciones por defecto: `{ temperature: 0.7, maxTokens: 128, stream: false }`
- `OLLAMA_TIMEOUT=30000` ms
- `OLLAMA_MODEL=tinyllama`
- `OLLAMA_HOST=http://localhost:11434`
## Configuración

---

- Mapeo: retornar objeto error con `code`, `message`, `correlationId`
- `LLM003 INVALID_OPTIONS`: opciones fuera de rango (p.ej., `maxTokens` negativo)
- `LLM002 TIMEOUT`: operación excede `OLLAMA_TIMEOUT`
- `LLM001 MODEL_NOT_FOUND`: modelo no disponible
## Errores y Códigos (LLM00X)

---

- Contadores por operación (futuro): número de inferencias por minuto
- `promptTokens`, `completionTokens`: requeridos cuando Ollama los exponga; si faltan → `metricsMissing=true` en logs
- `durationMs`: obligatorio
## Métricas

---

- `ChatMetrics(promptTokens?, completionTokens?, durationMs)`
- `OllamaChatResponse(model, response, done, metrics?)`
- `ChatOptions(temperature?, maxTokens?, stream=false)`
- `Message(role, content)`
- `OllamaChatRequest(model, messages[], options)`
## DTOs (borrador)

---

  4) Registrar logs con `correlationId`, duración y estado
  3) Capturar respuesta y normalizar métricas
  2) Enviar a Ollama con timeouts (`OLLAMA_TIMEOUT`)
  1) Construir request con `model`, `messages`, `options`
- Flujo:
  - `{ model, response, done, metrics?: { durationMs, promptTokens?, completionTokens? } }`
- Output:
  - `options`: `{ temperature?: number, maxTokens?: number, stream?: boolean=false }`
  - `messages`: `[{ role: 'system'|'user'|'assistant', content: string }]`
  - `model`: por defecto `tinyllama` (configurable)
- Input:
### ollama.chat(messages, options)

## Operaciones y Flujos

---

- Si en futuro se requiere seguridad local: evaluar header `Authorization` y lista blanca por IP.
  - `X-Profile`: perfil (`default|internal`) sólo para logging
  - `X-Role`: rol del cliente (`guest|agent|admin`) sólo para logging
  - `X-Correlation-Id`: correlación end-to-end
- Propagar contexto desde el BFF:
- Ollama local no requiere `apiKey` por defecto.
## Autenticación y Headers

---

- Integración: usado por `ChatService` del BFF.
- Responsabilidad: encapsular llamadas HTTP a Ollama (`/api/chat` o endpoint de SDK), gestionar opciones y retorno estandarizado.
- Ubicación: `apps/middleware/src/main/java/com/lujanita/bff/client/llm/OllamaClient.java`
## Arquitectura y Responsabilidad

---

Cliente del BFF para interactuar con Ollama embebido (modelo ligero). Provee operación de chat con métricas, opciones v1 y mapeo de errores.
## Resumen

# Plan de Implementación - 003 ollama-client

---
tags: [llm, ollama, client, bff, plan]
owner: Lujanita Team
last_updated: 2025-11-29
date_created: 2025-11-29
version: 1.0.0
title: 003 - Plan de Implementación ollama-client

---

## Prompts por rol/perfil (systemPrompt y userPromptOverrides)

Objetivo: permitir que el BFF construya el contexto previo y ajuste el prompt del usuario según `role` y `profile`, propagados desde los clientes.

Parámetros recibidos desde clientes (UI/BFF):
- `role`: `guest|agent|admin`
- `profile`: `default|internal|...`
- `systemPrompt`: texto del mensaje `system` (contexto previo)
- `userPromptOverrides`: objeto con ajustes (p.ej., idioma, estilo, formato)

Construcción de `messages` para `ollama.chat`:
1) Incluir `{ role: 'system', content: systemPrompt }`
2) Aplicar `userPromptOverrides` sobre el texto del usuario antes de generar `{ role: 'user', content: ... }`
3) Mantener mensajes previos de la sesión si aplica (fuera de alcance si no hay sesión global)

Ejemplo (entrada/salida):
- Entrada (del BFF hacia Ollama):
  ```json
  {
    "model": "tinyllama",
    "messages": [
      { "role": "system", "content": "Eres Lujanita. Responde en español y sé concisa." },
      { "role": "user", "content": "Estado del pedido SO001" }
    ],
    "options": { "temperature": 0.7, "maxTokens": 128, "stream": false }
  }
  ```
- Salida normalizada (del cliente):
  ```json
  {
    "model": "tinyllama",
    "response": "El pedido SO001 está confirmado y se entrega el 02/12.",
    "done": true,
    "metrics": { "durationMs": 350, "promptTokens": 42, "completionTokens": 18 }
  }
  ```

Observabilidad:
- Loguear `{ correlationId, role, profile, llmOperation: 'chat', durationMs }`
- No exponer contenido sensible del `systemPrompt` completo en logs (opcional: truncar/hashear)

Trazabilidad (FR-LLM-008):
- Este flujo cubre el requisito de aceptar y propagar `systemPrompt` y `userPromptOverrides` por rol/perfil.
- Validar en BDD que el `systemPrompt` se inyecta como mensaje `system` y los overrides se aplican al mensaje `user`.
