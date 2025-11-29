---
title: Integración con LLM Ollama (Embebido)
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [middleware, llm, ollama, sdd]
spec_phase: specify
---

# 003 - ollama-client

## Permalink: ollama-client

Diseñar la interacción del middleware con el servidor Ollama embebido (modelo liviano) para soporte conversacional e inferencias.

## 1. Propósito y Alcance

**Propósito**: Permitir al middleware delegar tareas de lenguaje natural (chat, clasificación de intención, generación breve) a Ollama.

**Alcance**:
- In scope:
  - Operación de chat: prompt y mensajes en formato `{ role, content }`
  - Opciones de inferencia: `temperature`, `maxTokens`, `stream=false`
  - Métricas básicas: tokens y duración
  - Manejo de errores específicos de Ollama (modelo no disponible, timeout)
- Out of scope:
  - Fine-tuning o carga dinámica avanzada de modelos
- Future scope:
  - Streaming (SSE/WebSocket)
  - Embeddings (`ollama.embed`) para búsqueda semántica

## 2. Definiciones

- Ollama: servidor local de LLM
- Modelo por defecto: `tinyllama` (o `phi-2` según configuración)

## 3. Historias de Usuario y Criterios

### US-LLM-001: Respuesta conversacional
Como middleware necesito generar una respuesta breve a partir de un mensaje.
- Given un mensaje `{ role: 'user', content: '...' }`
- When llamo a `ollama.chat`
- Then obtengo `{ response: '...', metrics: { durationMs, promptTokens, completionTokens } }`

### US-LLM-002: Manejo de errores de modelo
Como middleware necesito detectar si el modelo no está disponible y decidir fallback.
- Given `model=tinyllama` no disponible
- When llamo a `ollama.chat`
- Then recibo error `MODEL_NOT_FOUND` y registro alerta

## 4. Requisitos Funcionales

- FR-LLM-001: Definir DTOs de request/response para chat con `messages` y `options`.
- FR-LLM-002: Permitir configuración de `model` (`tinyllama` por defecto) y `temperature`, `maxTokens`.
- FR-LLM-003: Retornar métricas básicas en la respuesta.
- FR-LLM-004: Mapear y propagar errores de Ollama a códigos `LLM00X`.
- FR-LLM-005: Logs estructurados con `correlationId`.
- FR-LLM-006: Incluir `durationMs` siempre y `promptTokens`/`completionTokens` cuando estén disponibles; si faltan, registrar `metricsMissing=true` en logs.
- FR-LLM-007: Opciones avanzadas (`top_p`, `frequency_penalty`) fuera de alcance en v1.

## 5. Criterios de Éxito

- SC-LLM-001: 95% de inferencias en < 2.0s con `tinyllama`.
- SC-LLM-002: Errores de disponibilidad y tiempo claramente registrados.
- SC-LLM-003: Métricas disponibles para monitoreo básico.

## 6. Entidades

- OllamaChatRequest: `{ model?: string, messages: Message[], options?: ChatOptions }`
- Message: `{ role: 'system'|'user'|'assistant', content: string }`
- ChatOptions: `{ temperature?: number, maxTokens?: number, stream?: boolean }`
- OllamaChatResponse: `{ model: string, response: string, done: boolean, metrics?: ChatMetrics }`
- ChatMetrics: `{ promptTokens?: number, completionTokens?: number, durationMs?: number }`

## 7. Supuestos

- Ollama accesible en `http://localhost:11434`
- Modelos `tinyllama` y `phi-2` disponibles para pruebas

## 8. Dependencias

- Cliente `ollama4j`
- Configuración en `application.yml` del middleware

## 9. Riesgos

- Latencia mayor en CPU: ajustar `maxTokens`
- Modelos no disponibles: pre-carga y validación en health

## 10. Aceptación (Resumen)

- `ollama.chat` retorna respuesta y métricas
- Errores mapeados correctamente (`MODEL_NOT_FOUND`, `OLLAMA_TIMEOUT`)

## 11. [NEEDS CLARIFICATION]

1) Modelo por defecto:
   - [RESUELTO] `tinyllama` será el modelo por defecto en todos los entornos. `phi-2` podrá configurarse manualmente en entornos con más recursos.
2) Métricas mínimas:
   - [RESUELTO] `promptTokens` y `completionTokens` serán obligatorias en la respuesta cuando el proveedor (Ollama) las exponga; en caso de indisponibilidad, se registrará `metricsMissing=true` en logs, manteniendo `durationMs` siempre presente.
3) Opciones soportadas:
   - [RESUELTO] En v1 sólo se soportan `temperature`, `maxTokens` y `stream=false`; opciones avanzadas como `top_p` y `frequency_penalty` quedan fuera de alcance.

## 12. Decisiones de Clarificación

- Modelo por defecto:
  - `model`: `tinyllama` por defecto; `phi-2` opcional vía configuración del BFF/middleware.
- Métricas mínimas:
  - `durationMs` siempre presente.
  - `promptTokens` y `completionTokens` requeridas cuando disponibles; si faltan, se registra en logs el flag `metricsMissing=true`.
- Opciones v1:
  - Soportadas: `temperature`, `maxTokens`, `stream` (por defecto `false`).
  - No soportadas: `top_p`, `frequency_penalty`.

## 13. Calidad de Especificación

Especificación atómica, orientada a integración clara y medible.
