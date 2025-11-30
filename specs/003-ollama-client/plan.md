# Plan 003 - Cliente Ollama

## Decisiones Arquitectónicas
1. Acceso a Ollama vía HTTP streaming (endpoint `/api/generate` o equivalente) en lugar de dependencia fuerte de librería, para reducir acoplamiento y facilitar mocks.
2. Uso de WebClient (Spring Reactive) para manejar streaming y timeouts configurable.
3. Plantillas de prompt cargadas desde directorio `config/ollama/prompts/` con watcher (timestamp + cache local).
4. Estrategia de fallback: si `onError` o no llega evento `done` en `streamTimeoutMs`, cancelar y ejecutar request sin streaming.
5. CorrelationId: recibido desde capa BFF externa; si no existe generar (UUID v4) en pre-procesamiento.
6. Métricas: Micrometer + meterRegistry (timers, distribution summaries, counters).
7. Sanitización: lista corta de patrones a reemplazar (caracteres de control, secuencias repetitivas > N). Se implementa filtro antes del envío.
8. Concurrencia: limitar máximo `maxConcurrentStreams` (Semaphore interna). Si excede, rechazar con LLM003 (STREAM_INTERRUPTED / capacity) o esperar (configurable).
9. HealthCheck: petición ligera al endpoint `/api/tags` para confirmar modelos disponibles en warmup y luego cada intervalo.
10. Config extensible para mapping role/profile→model (archivo `model-mapping.yaml`).

## Componentes
- `OllamaClient`: fachada principal (métodos: `generate(promptCtx)`, `stream(promptCtx, consumer)`, `fallbackGenerate(promptCtx)`, `healthCheck()`).
- `PromptBuilder`: construye y valida prompt completo.
- `TemplateLoader`: carga y cachea plantillas, detecta cambios.
- `ModelSelector`: resuelve modelo según role/profile.
- `MetricsRecorder`: encapsula registro Micrometer.
- `ErrorMapper`: traduce excepciones a códigos LLM00X.

## Flujos Principales
### Generar con Streaming
1. BFF construye contexto → `PromptBuilder.build()`.
2. `ModelSelector.select(role, profile)` obtiene modelo.
3. `OllamaClient.stream()` abre conexión HTTP streaming.
4. Emisión de fragmentos → callback `consumer.onChunk(text)`.
5. Timeout o error → fallback.
6. Final `consumer.onComplete(meta)` con tokens contados.

### Fallback
1. Abort streaming (cancel token).
2. Ejecutar `generate()` (modo completo).
3. Registrar `stream_fallback=true`.

### Template Reload
1. En cada build si `lastModified` > `cachedModified` recargar.
2. Validar schema (estructura mínima: `systemTemplate`, `roleOverlays`).

## Configuración
- `ollama.baseUrl`
- `ollama.streamTimeoutMs`
- `ollama.maxPromptChars`
- `ollama.maxConcurrentStreams`
- `ollama.templateDir`
- `ollama.modelMappingFile`

## Errores Esperados
- Timeout → `LLM001`
- Modelo inexistente → `LLM002`
- Stream interrumpido/capacidad → `LLM003`
- Archivo de plantilla inválido → `LLM004`
- Fallback fallido → `LLM005`
- Desconocido → `LLM099`

## Estrategia de Pruebas (Resumen, detalle en tasks.md)
- Unit: PromptBuilder, TemplateLoader, ErrorMapper.
- Integration: stream vs fallback (WireMock simulando Ollama).
- BDD: Escenarios generados en fase 0.5 (tags FR-300x, @stream, @fallback, @error-timeout).

## Observabilidad
- Timer: `ollama_request_latency`
- Distribution summary: `ollama_tokens_out`
- Counter: `ollama_stream_fallbacks`
- Gauge opcional: `ollama_active_streams`

## Seguridad
- No se envían credenciales del usuario al LLM, solo contexto filtrado.
- Sanitizar prompt evita inyección de secuencias de control.

## Riesgos Mitigación
- Plantilla corrupta → validación schema + fallback a versión previa.
- Saturación streams → Semaphore.
- Alto costo latencia → monitoreo p95 y ajuste de modelo.

## Próximas Extensiones
- Incorporar embeddings y retrieval augment (fuera de esta release).
- Cache local de respuestas frecuentes.

