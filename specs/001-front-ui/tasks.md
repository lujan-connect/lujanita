# Tasks - 001 Front UI (mobile-first + widget)

## TASK-001: Generar Gherkin de embedding y theming
- Ubicación: `apps/widget/features/chat_widget_embedding.feature`
- Accept: Escenarios ejecutables Given-When-Then

## TASK-002: Generar Gherkin de envío de mensaje al BFF
- Ubicación: `apps/widget/features/chat_send_message.feature`
- Accept: POST /api/chat con headers requeridos

## TASK-003: Generar Gherkin de manejo de errores
- Ubicación: `apps/widget/features/chat_error_handling.feature`
- Accept: UI003 y UI006 visibles

## TASK-004: Generar Gherkin de persistencia de sesión
- Ubicación: `apps/widget/features/chat_session_persistence.feature`
- Accept: Restauración desde sessionStorage

## TASK-005: Generar Gherkin de accesibilidad e i18n
- Ubicación: `apps/widget/features/chat_accessibility_i18n.feature`
- Accept: Navegación por teclado y textos desde traducciones

## TASK-006: Generar Gherkin de streaming (SSE/WebSocket)
- Ubicación: `apps/widget/features/chat_streaming.feature`
- Accept: Render incremental vía SSE y fallback a REST documentado

## Notas
- Todos los features deben incluir tags Jira, prioridad y FR.
- Red phase: los tests BDD deben fallar inicialmente.
