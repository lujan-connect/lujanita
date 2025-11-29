# Especificación 003 - Cliente Ollama (ollama-client)
(placeholder)
## Lecciones Aprendidas (Se actualizará al finalizar)

- US-003-06 → FR-3008, FR-3010
- US-003-05 → FR-3007
- US-003-04 → FR-3005, FR-3006
- US-003-03 → FR-3004
- US-003-02 → FR-3002, FR-3003
- US-003-01 → FR-3001, FR-3009, FR-3012
User Stories ↔ FR:
## Trazabilidad

- Config central (Spring Boot `@ConfigurationProperties`).
- Sistema de archivos para cargar plantillas.
- Librería: `ollama4j` (o HTTP directo). Decisión en `plan.md`.
## Dependencias

- Modelos no disponibles generan cascada de fallos (healthCheck proactivo en warmup).
- Plantillas mal formadas causan prompts inválidos (agregar validación JSON/YAML schema).
- Sobrecarga si múltiples streams simultáneos (mitigar con límites de concurrentes).
## Riesgos

- Cambiar una plantilla en disco se refleja en la siguiente llamada sin reinicio (lazy reload detectado por timestamp).
- Logs contienen `correlationId` y códigos de error correctos.
- En falla simulada de streaming se observa fallback y respuesta completa final.
- Se reciben eventos streaming y el último evento incluye flag `done=true` y conteo total de tokens.
## Criterios de Aceptación

- NFR-3004: Cobertura de pruebas unitarias ≥ 80% para lógica de construcción de prompt y manejo de errores.
- NFR-3003: Recuperación ante fallo de streaming < 500ms adicional.
- NFR-3002: El cliente no debe bloquear el hilo principal (usar async/reactive o thread pool controlado).
- NFR-3001: Latencia p95 < 1500ms para modelos ligeros en condiciones normales.
## Requisitos No Funcionales (NFR)

- FR-3012: Incluir función de sanitización básica (remover secuencias peligrosas) antes de envío.
- FR-3011: Exponer método para pre-chequear disponibilidad del modelo (`healthCheck()`).
- FR-3010: Soportar selección de modelo por role/profile (tabla de mapeo interna).
- FR-3009: Validar longitud de prompt: si supera límite configurado (`maxPromptChars`), truncar contexto histórico y emitir warning.
- FR-3008: Plantillas deben poder cargarse desde archivo YAML/JSON caliente (directorio configurable) sin reinicio.
- FR-3007: Mapear errores a códigos: TIMEOUT→LLM001, MODEL_NOT_FOUND→LLM002, STREAM_INTERRUPTED→LLM003, UNKNOWN→LLM099.
- FR-3006: Incluir en todos los logs: `correlationId`, `llmModel`, `role`, `profile`.
- FR-3005: Registrar métricas: `ollama_latency_ms`, `ollama_tokens_in`, `ollama_tokens_out`, `ollama_prompt_chars`.
- FR-3004: Si streaming falla (error o timeout configurado), invocar automáticamente modo completo (fallback) y loggear evento `stream_fallback=true`.
- FR-3003: En streaming, debe emitir eventos internos con fragmentos y marcar fin con `done`.
- FR-3002: Debe soportar modo streaming y modo completo (flag `stream=true/false`).
- FR-3001: El cliente debe construir el prompt concatenando: `systemTemplate(role, profile)` + `userMessage` + contexto adicional (opcional histórico último N).
## Requisitos Funcionales (FR)

"Como administrador quiero poder definir diferentes versiones de plantillas de prompt por role/profile sin redeploy completo del BFF."
### US-003-06: Versionado de plantillas

"Como desarrollador del BFF quiero recibir códigos de error LLM00X claros cuando Ollama falle (timeout, invalid model, rate limit interno, etc.)."
### US-003-05: Manejo de errores estandarizado

"Como ingeniero de observabilidad quiero registrar latencia, cantidad de tokens y tamaño del prompt para monitoreo y alertas."
### US-003-04: Métricas y logs

"Como servicio BFF quiero que si falla el streaming (timeout o desconexión), se realice un pedido de respuesta completa y se entregue al cliente."
### US-003-03: Fallback a respuesta completa

"Como servicio BFF necesito recibir la respuesta desde Ollama token a token para reenviarla en streaming al widget."
### US-003-02: Streaming de tokens

"Como servicio BFF quiero enviar un prompt a Ollama que combine la plantilla base y el contexto del usuario (role/profile) para obtener una respuesta personalizada."
### US-003-01: Solicitud de respuesta contextual
## User Stories

- Equipo de datos (observabilidad / métricas).
- Equipo de producto (definición de roles y perfiles).
- Equipo de backend (BFF).
## Stakeholders

- Moderación de contenido (se asume fuera en esta iteración).
- Cache vectorial externa.
- Fine-tuning de modelos.
- Gestión avanzada de embeddings.
## Fuera de Alcance

Incluye únicamente la capa de cliente hacia Ollama y su integración directa en el BFF. No cubre UI ni orquestación con MCP Odoo (eso será parte de la spec 004). Se limita a modelos ligeros embebidos (p.ej. `llama3:8b-instruct` o similar).
## Alcance

- Observabilidad (latencia, tamaño del prompt, tokens generados, coste estimado si aplica).
- Manejo de errores con códigos normalizados (LLM00X).
- Métricas y trazabilidad (`correlationId`).
- Streaming de tokens y fallback a respuesta completa.
- Plantillas de prompt configurables y versionadas.
- Contexto dinámico por `apiKey`, `role` y `profile`.
El objetivo de esta feature es proveer un cliente robusto dentro del BFF (antes llamado middleware) para interactuar con el modelo LLM embebido de Ollama en la VM, soportando:
## Resumen


