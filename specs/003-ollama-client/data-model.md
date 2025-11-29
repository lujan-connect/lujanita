# data-model.md - 003-ollama-client

## Esquema de entidades Ollama

- LlmPrompt: { context, prompt, role, profile }
- LlmResponse: { text, correlationId, model, latencyMs }

## Relaciones
- Un LlmPrompt produce un LlmResponse

