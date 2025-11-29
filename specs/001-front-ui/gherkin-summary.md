# Gherkin Summary - Spec 001 Front UI

## Files
- apps/widget/features/chat_widget_embedding.feature
- apps/widget/features/chat_send_message.feature
- apps/widget/features/chat_error_handling.feature
- apps/widget/features/chat_session_persistence.feature
- apps/widget/features/chat_accessibility_i18n.feature
- apps/widget/features/chat_streaming.feature

## Scenarios Count
- Embedding: 2
- Send message: 2
- Error handling: 2
- Session persistence: 1
- Accessibility/i18n: 2
- Streaming: 2

## Tags
- Jira: @JIRA:LUJ-001 @JIRA:LUJ-002 @JIRA:LUJ-003 @JIRA:LUJ-004 @JIRA:LUJ-005 @JIRA:LUJ-006
- Priority: @P1, @P2
- Requirements: @FR-001, @FR-003, @FR-004, @FR-005, @FR-006, @FR-007, @FR-008, @FR-010, @FR-011, @FR-012, @FR-013

## Initial Command (expected to fail)
- cd apps/widget && npm run test:bdd -- features/chat_widget_embedding.feature
- cd apps/widget && npm run test:bdd -- features/chat_send_message.feature
- cd apps/widget && npm run test:bdd -- features/chat_error_handling.feature
- cd apps/widget && npm run test:bdd -- features/chat_session_persistence.feature
- cd apps/widget && npm run test:bdd -- features/chat_accessibility_i18n.feature
- cd apps/widget && npm run test:bdd -- features/chat_streaming.feature
