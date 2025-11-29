---
title: 001 - Plan de Implementación UI (App mobile-first + Widget embebible)
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [frontend, react, widget, bff, plan]
---

# Plan de Implementación - UI Frontend (mobile-first + widget)

## Resumen
Implementar una UI en React compuesta por:
- App mobile-first (SPA) para uso interno/desktop/móvil
- Widget embebible (script + componente) para landing/home

Ambas se comunican con el BFF vía REST, con headers `apiKey`, `role`, `profile`, `correlationId`. 
Incluye i18n, accesibilidad, observabilidad y manejos de error `UI00X`.

---

## Arquitectura Técnica

- Framework: React 18 + Vite (TypeScript)
- Estructura monorepo:
  - `apps/widget/` (UI + widget)
  - `apps/widget/src/` componentes, hooks y servicios
  - `apps/widget/src/i18n/` internacionalización (ES/EN)
  - `apps/widget/mocks/` datos de mock para tests
- Comunicación:
  - REST al BFF (`POST /api/chat`, `GET /health`)
  - Headers: `apiKey`, `role`, `profile`, `correlationId`
- Observabilidad:
  - Logs en consola: `{ correlationId, operation, status }`
  - Mapeo de errores: `UI00X`
- Accesibilidad:
  - Roles ARIA, navegación por teclado, contraste suficiente
  - Controles y foco gestionados
- Seguridad:
  - No persistir `apiKey` en storage
  - Sanitizar contenido de mensajes

---

## Decisiones Clave

1) Mobile-first y Responsive
- Usar CSS variables y utilidades (p.ej. CSS modules o Tailwind si se introduce más adelante)
- Breakpoints: 360px, 768px, 1024px

2) Widget embebible
- Exponer `LujanitaWidget.init({ apiKey, role, profile, endpoint, theme })`
- Render en contenedor designado (`#lujanita-widget` o pasado por config)
- Aislar estilos mediante un prefijo de clase raíz (`.lujanita-widget`) y CSS scope

3) Configuración y theming
- `ChatConfig`: `{ apiKey, role, profile, endpoint }`
- `theme`: `{ primaryColor, backgroundColor, logoUrl? }`
- No persistir claves en storage, sólo en memoria

4) Persistencia de sesión
- Usar `sessionStorage` (`lujanita:session:<siteId>`) para `sessionId` y mensajes
- Al recargar, recuperar conversación y continuar

5) Internacionalización
- Claves en inglés camelCase (p.ej. `sendMessage`, `errorForbidden`)
- Traducciones ES/EN en `apps/widget/src/i18n/translations.ts`
- No hardcodear texto visible

6) Observabilidad y errores
- Agregar `correlationId` por request y loguearlo
- Mapear errores HTTP a UI00X: 400→UI001, 401→UI002, 403→UI003, 404→UI004, 5xx→UI005, timeout→UI006

---

## Interfaces y Contratos (UI ↔ BFF)

- `POST /api/chat`
  - Request: `{ message: string, sessionId?: string }`
  - Headers: `apiKey`, `role`, `profile`, `correlationId`
  - Response: `{ response: string, correlationId: string, intent?: string, entities?: Record<string,string> }`

- `GET /health`
  - Response: `{ status: 'UP'|'DOWN', version: string, components: { ollama: { status, model }, odoo: { status, latencyMs } } }`

---

## Diseño de Componentes

- `ChatWidget` (widget embebible)
  - Props: `config: ChatConfig`, `theme?: Theme`
  - Estado: `messages: ChatMessage[]`, `sessionId`, `loading`, `errorUI?: ErrorUI`
  - Acciones: `sendMessage`, `retry`, `clear`

- `ChatApp` (app mobile-first)
  - Layout responsivo y compartición de lógica con `ChatWidget` vía hooks

- Hooks
  - `useChat(config)` → maneja sesión, headers, llamadas al BFF y errores
  - `useCorrelationId()` → genera y propaga `correlationId`

- Servicios
  - `chatService.postMessage(config, message)`
  - `healthService.getStatus(config)`

---

## Testing (Test-First)

- BDD (Cucumber) para escenarios críticos en `apps/widget/features/`
- Unit (Vitest + Testing Library)
- Mocks en `apps/widget/mocks/`
- Cobertura mínima: 80% para nuevo código

Casos
- Envío de mensaje con headers → render de respuesta
- Manejo de error 403 → mostrar `UI003`
- Persistencia de sesión → mensajes sobreviven recarga
- i18n → textos provenientes de traducciones

---

## Observabilidad

- Logs estructurados en consola:
  - `{ timestamp, correlationId, operation, status, durationMs? }`
- Métricas (futuro): latencia por operación, tasa de error

---

## Riesgos y Mitigaciones

- Colisión de estilos en páginas externas → prefijo y scope
- Latencia/timeout del BFF → estados de loading y reintentos controlados
- Accesibilidad insuficiente → validación con checklist y auditoría

---

## Gate de Calidad para Plan

- [x] Alineado con `spec.md` (BFF, configuraciones, i18n, accesibilidad, observabilidad)
- [x] Define contratos y headers
- [x] Lista componentes, hooks y servicios
- [x] Estrategia de testing y escenarios
- [x] Riesgos y mitigaciones

## Próximos Pasos

1) Generar feature files Gherkin (speckit.gherkin) para la Spec 001
2) Implementar tests BDD (deben fallar) y unitarios
3) Implementación mínima hasta verde
4) Documentar cualquier patrón nuevo en `.github/copilot-knowledge/`

