# Testing Guide - Lujanita SDD

**Propósito**: Alinear estrategias de testing para el widget React y el middleware Java que conectan a Lujanita con Odoo vía MCP.

---

## Pirámide de Pruebas
```
    ┌──────────┐
    │ Convers. │ (E2E: flujo completo del chatbot)
    ├──────────┤
    │ Integr.  │ (Contratos MCP, llamadas Odoo, APIs intermedias)
    ├──────────┤
    │ Unit     │ (Hooks React, servicios Java, DTOs)
    └──────────┘
```

## Reglas Generales
1. Test-first obligatorio (BDD + unit tests).
2. Cada feature nueva debe registrar fallas iniciales (cucumber/vitest o JUnit) antes de implementar.
3. Logs de pruebas deben conservar `correlationId` para depurar interacciones MCP.
4. Escenarios dados en `spec.md` → replicar en Gherkin y coverage unitario.

## Widget React (apps/widget)
- **BDD**: `npm run test:bdd` (Cucumber + Testing Library).
- **Unit**: `npm run test` (Vitest en modo jsdom).
- **Patrones**:
  - Steps en `apps/widget/steps/*_steps.ts` reutilizan hooks y helpers documentados.
  - Mocks externos en `apps/widget/mocks/` para evitar llamadas reales al middleware.
  - Respetar i18n: validar claves traducidas (usar `t('key')`).

## Middleware Java (apps/middleware)
- **BDD**: `./mvnw testBdd` (Cucumber + Spring Boot + RestAssured).
- **Unit/Integration**: `./mvnw test` (JUnit 5 + Mockito + WireMock).
- **Patrones**:
  - Step definitions en `apps/middleware/src/test/java/.../steps` deben simular MCP server.
  - WireMock para endpoints MCP/Odoo definidos en `packages/contracts/mocks`.
  - Métricas y logs validados mediante asserts sobre `MeterRegistry` o appenders de log.

## Gherkin Checklist
- Tags obligatorios: `@JIRA:LUJ-XXX`, `@FR-XXX`, prioridad.
- Escenarios Given-When-Then reflejan exacto el flujo de usuario.
- `Background` solo si reduce duplicación real.

## Fases de Ejecución
1. `cd apps/widget && npm run test:bdd` (esperado: falla inicial).
2. `cd apps/middleware && ./mvnw testBdd` (esperado: falla inicial).
3. Implementación mínima → repetir comandos hasta pasar.
4. `npm run test` y `./mvnw test` para validar unidad/integración.

## Evidencia en Pull Requests
- Adjuntar salida resumida de comando BDD + unitario.
- Registrar versión del contrato MCP usado.
- Señalar escenarios omitidos y justificar.

Mantén este documento actualizado cuando cambien flujos MCP u ONBOARDING del widget. Se debe revisar en cada retro de release.
