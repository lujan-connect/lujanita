# Lujanita Widget - BDD

## Ejecutar pruebas BDD (deben fallar inicialmente)

```bash
cd apps/widget
npm run test:bdd -- features/chat_widget_embedding.feature
npm run test:bdd -- features/chat_send_message.feature
npm run test:bdd -- features/chat_error_handling.feature
npm run test:bdd -- features/chat_session_persistence.feature
npm run test:bdd -- features/chat_accessibility_i18n.feature
npm run test:bdd -- features/chat_streaming.feature
```

## Implementar Steps
- Ubicación: `apps/widget/steps/*_steps.ts`
- Usar Testing Library para simular interacción y asserts
- Mantener logs con correlationId en consola

## Notas
- Seguir SDD: tests primero (rojo), luego implementación mínima
- i18n: claves camelCase, traducciones ES/EN
- No persistir apiKey en storage
