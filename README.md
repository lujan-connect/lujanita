# Lujanita

Chatbot oficial de Lujan de Cuyo Express diseñado para asistir a clientes y operadores internos sobre disponibilidad, reservas y seguimiento logístico.

## Arquitectura
- **Widget React** (`apps/widget/`): componente embebible que expone la experiencia conversacional de Lujanita.
- **Middleware Java** (`apps/middleware/`): servicio Spring Boot (Maven) que actúa como cliente MCP frente a Odoo y cliente de Ollama para capacidades LLM.
- **Ollama** (`platform/ollama/`): LLM embebido en VM (TinyLlama/Phi-2) para procesamiento de lenguaje natural.
- **Odoo** (`platform/odoo/`): origen de verdad para órdenes, contactos y workflows; aloja el MCP Server utilizado por el middleware.

## Flujo SDD (Spec-Driven Development)
1. Especifica cada feature bajo `specs/<id>-<feature>/spec.md` usando el toolkit oficial de GitHub Spec Kit.
2. Genera features en Gherkin con los prompts de `.github/prompts/` y publícalos en la carpeta correspondiente (`apps/widget/features/`, `apps/middleware/features/`).
3. Implementa siempre test-first: Cypress/Vitest para el widget React y JUnit/Cucumber para el middleware Java.
4. Integra con Odoo únicamente mediante contratos MCP documentados en `packages/contracts/`.

## Scripts útiles
```bash
npm install             # Dependencias del widget (desde apps/widget)
./scripts/dev-setup.sh  # Configura toolchains, ganchos y prompts SDD
```

> Toda contribución nueva debe incluir especificación aprobada, tests automatizados y trazabilidad a una feature taggeada.
