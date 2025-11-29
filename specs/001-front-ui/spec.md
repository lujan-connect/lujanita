---
title: Frontend UI - Mobile-first App & Embeddable Widget
version: 1.0.0
date_created: 2025-11-29
last_updated: 2025-11-29
owner: Lujanita Team
tags: [frontend, react, widget, mobile-first, sdd]
spec_phase: specify
---

# Frontend UI - Mobile-first App & Embeddable Widget

## Permalink: frontend-ui-mobile-widget

Diseñar la UI del chatbot Lujanita con dos formatos: (1) una aplicación React mobile-first y (2) un widget embebible para páginas landing/home de la empresa. Ambos deben comunicarse con el BFF del proyecto y ser configurables (apikey, rol, perfil).

## 1. Purpose & Scope

**Purpose**: Proveer una UI moderna, accesible y embebible para interactuar con Lujanita, conectada de forma segura al BFF.

**Scope**:
- In scope:
  - App React mobile-first (SPA) para uso en móviles y escritorio
  - Widget embebible (script + componente) que se integra en páginas existentes
  - Configuración mediante apikey, rol y perfil para conectar con el BFF
  - Comunicación con el BFF vía REST y streaming (SSE/WebSocket) para respuestas fluídas tipo chat
  - Internacionalización (ES como base, soporte EN/ES)
  - Observabilidad básica (correlationId y códigos de error UI00X)
- Out of scope:
  - Lógica del BFF u Odoo
  - Diseño visual definitivo (se usarán componentes mínimos y tokens de estilo)
  - Autenticación compleja (SSO, OAuth) más allá de apikey/rol/perfil provistos
- Future scope:
  - Theming avanzado y UI kit compartido (`packages/ui/`)
  - Integración con analíticas y métricas de uso

**Target Users**:
- Usuarios finales en landing/home (cliente general)
- Operadores internos (modo app)
- Administradores para configuración básica

**Business Goals**:
- Aumentar conversión y autoservicio desde el sitio
- Reducir carga de operadores en consultas simples
- Facilitar despliegue rápido en distintas páginas

## 2. Definitions

- Widget embebible: componente React exportado como script que se inserta en páginas existentes
- Mobile-first: diseño que prioriza experiencia en pantallas pequeñas
- BFF: Backend for Frontend del proyecto (Spring Boot, puerto 9000) con APIs para el chatbot
- Configuración: parámetros de conexión (apikey, rol, perfil) provistos para cada instancia

## 3. User Stories & Acceptance Criteria (BDD-style)

### US-001: Ver conversación en widget embebible
Como visitante del sitio quiero abrir el widget de Lujanita y enviar un mensaje para recibir asistencia.

- Given el widget está embebido en la landing y configurado con apikey/rol/perfil
- When el usuario hace clic en el botón del widget y envía "¿Estado de mi pedido?"
- Then el widget muestra la conversación y la UI indica el envío al BFF con un correlationId visible en logs (no UI)

### US-002: UI mobile-first responsiva
Como usuario móvil quiero que la UI se adapte a mi pantalla sin pérdida de funcionalidad.

- Given la app se abre en un dispositivo de 360-400px de ancho
- When el usuario escribe y envía un mensaje
- Then los controles son accesibles, la tipografía legible y no hay overflow horizontal

### US-003: Configuración por apikey/rol/perfil
Como administrador quiero parametrizar la conexión al BFF por apikey, rol y perfil.

- Given el widget recibe `{ apiKey, role, profile }`
- When se inicializa la instancia
- Then todas las llamadas al BFF incluyen estos parámetros y se valida el acceso

### US-004: Errores visibles y manejados
Como usuario quiero recibir mensajes claros si hay errores.

- Given el BFF retorna 403 por rol inválido
- When el widget recibe la respuesta
- Then la UI muestra un error amigable con código `UI003` y sugiere reintentar

## 4. Functional Requirements (FR)

- FR-001: El widget debe inicializarse mediante un script único y renderizar el componente React en un contenedor designado.
- FR-002: La app React debe ser mobile-first y responsiva (breakpoints mínimos: 360px, 768px, 1024px).
- FR-003: La configuración mínima por instancia incluye `apiKey` (string), `role` (string), `profile` (string) y `endpoint` (URL del BFF).
- FR-004: Todas las peticiones al BFF deben incluir `apiKey`, `role`, `profile` y `correlationId` en headers.
- FR-005: Internacionalización: claves en inglés camelCase con traducciones ES/EN.
- FR-006: Observabilidad: logs estructurados en consola con `correlationId`, `operation`, `status`.
- FR-007: Manejo de errores: mapear respuestas HTTP a códigos `UI00X` (400→UI001, 401→UI002, 403→UI003, 404→UI004, 5xx→UI005, timeout→UI006).
- FR-008: Accesibilidad: soporte teclado y ARIA roles básicos para el componente de chat.
- FR-009: Seguridad: no persistir `apiKey` en almacenamiento permanente; evitar exposición en DOM.
- FR-010: Theming básico por instancia: `primaryColor`, `backgroundColor`, `logoUrl`.
- FR-011: Persistencia de conversación en `sessionStorage` durante la sesión actual.
- FR-012: Conectividad v1 vía REST y streaming SSE (con fallback a REST) hacia el BFF.
- FR-013: El widget y la app deben renderizar respuestas de manera incremental (chunked) cuando el BFF emita eventos de servidor (SSE) o datos vía WebSocket.

## 5. Success Criteria

- SC-001: Tiempo de render inicial del widget < 1.5s en dispositivos móviles de gama media.
- SC-002: 95% de interacciones básicas (enviar mensaje/recibir respuesta) completan en < 2.5s.
- SC-003: El widget se integra en al menos 2 páginas diferentes sin cambios de código.
- SC-004: La app pasa auditoría básica de accesibilidad (navegación por teclado, labels, contraste).
- SC-005: Logs muestran `correlationId` en 100% de llamadas al BFF durante pruebas.

## 6. Key Entities

- ChatConfig: `{ apiKey: string, role: string, profile: string, endpoint: string }`
- ChatMessage: `{ id: string, role: 'user'|'assistant'|'system', content: string, timestamp: string }`
- ChatSession: `{ sessionId: string, messages: ChatMessage[], createdAt: string }`
- ErrorUI: `{ code: 'UI00X', messageKey: string, details?: any }`

## 7. Assumptions

- Se utilizará React 18 con Vite para la app y el widget.
- La comunicación inicial será vía REST (POST /api/chat) contra el BFF; streaming se evaluará más adelante.
- El BFF validará `apiKey/role/profile` y retornará errores estandarizados.

## 8. Dependencies

- BFF operativo con endpoint `/api/chat` y health `/health`.
- Sistema de i18n base disponible en `apps/widget/src/i18n` (a crear durante implementación).

## 9. Risks & Mitigations

- Widget en páginas con frameworks distintos: encapsular estilos y evitar colisiones CSS.
- Exposición de `apiKey`: manejar sólo en headers y evitar logging del valor.
- Rendimiento en móviles: limitar bundle y usar lazy-loading cuando sea posible.

## 10. Acceptance Scenarios (Summary)

- AS-001: Embedding
  - Dado snippet `<script src="/widget.js"></script>` y `<div id="lujanita-widget"></div>`
  - Cuando `LujanitaWidget.init({ apiKey, role, profile, endpoint })`
  - Entonces el componente renderiza y permite enviar/recibir mensajes

- AS-002: Mobile-first
  - Dado viewport 375x667
  - Cuando el usuario escribe y envía
  - Entonces layout responsivo sin overflow y controles accesibles

- AS-003: Errores
  - Dado respuesta 403 del BFF
  - Cuando se procesa
  - Entonces mostrar `UI003` traducido y opción de reintento

## 11. [NEEDS CLARIFICATION]

1) Alcance de el theming:
   - [RESUELTO] En v1 se requiere theming básico por instancia: color primario, color de fondo y logotipo opcional (URL). No se incluyen tipografías personalizadas ni temas avanzados.
2) Persistencia de sesión:
   - [RESUELTO] Se debe persistir la sesión/conversación localmente en `sessionStorage` (no `localStorage`) para continuidad entre recargas dentro de la misma sesión del navegador.
3) Conectividad avanzada:
   - [RESUELTO] En v1 solo REST (POST /api/chat). Streaming (SSE/WebSocket) queda fuera de alcance y se evaluará en una futura spec.

## 12. Decisiones de Clarificación

- Theming v1:
  - `theme`: `{ primaryColor: string, backgroundColor: string, logoUrl?: string }` por instancia en el widget/app.
  - Se aplican estilos encapsulados para evitar colisiones en páginas externas.
- Persistencia de sesión:
  - `sessionStorage` con clave `lujanita:session:<siteId>` para mensajes y `sessionId`.
  - No se persistirá `apiKey`/`role`/`profile` en storage.
- Conectividad v1:
  - REST y streaming inicial vía SSE; si no es posible, fallback automático a REST.
  - Headers incluyen `apiKey`, `role`, `profile`, `correlationId`.
