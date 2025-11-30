# Research 003 - Cliente Ollama

## Opciones de Integración
1. Librería `ollama4j`:
   - Ventajas: abstracción simple, manejo de modelos.
   - Desventajas: dependencia adicional, menor control fino de streaming.
2. HTTP directo (WebClient):
   - Ventajas: control total, fácil mock con WireMock, menos dependencia.
   - Desventajas: más código boilerplate.

## Decisión
Elegimos HTTP directo (ver plan.md) para controlar timeouts, reintentos y métricas finas sin capa intermedia.

## Endpoints Relevantes Ollama (según documentación pública)
- `POST /api/generate` (puede soportar streaming: respuesta chunked con tokens).
- `GET /api/tags` (modelos disponibles).

## Modelos Candidatos
- `llama3:8b-instruct` (balance capacidad y latencia) p95 esperado ~1200ms.
- `mistral:7b-instruct` como alternativa fallback.

## Métricas Esperadas
- Tokens promedio por respuesta < 200.
- Prompt chars promedio < 3000 (límite configurado p.ej. 4000).

## Comparativa Simulación (Estimación)
| Opción | Latencia p95 | Control streaming | Complejidad | Dependencia |
|--------|--------------|-------------------|-------------|-------------|
| ollama4j | ~ similar base | Media | Baja | Sí |
| HTTP directo | Igual | Alta | Media | No |

## Riesgos Técnicos
- Cambios de versión Ollama podrían alterar formato de streaming → Mitigar con contract tests.
- Manejo de backpressure si tokens muy rápidos → utilizar buffer interno con tamaño máximo.

## POCs Sugeridos
1. Simular streaming con WireMock chunked response.
2. Validar recarga de plantilla modificando timestamp del archivo.
3. Medir latencia local vs remota (VM) para confirmar presupuesto p95.

## Referencias
- Documentación Ollama oficial.
- Ejemplos streaming HTTP chunked en Spring WebClient.

