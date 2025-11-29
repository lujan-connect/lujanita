# Gherkin Summary - Spec 002 BFF API REST

## Files
- apps/middleware/features/bff_chat_api.feature
- apps/middleware/features/bff_orders_api.feature
- apps/middleware/features/bff_health_api.feature

## Scenarios Count
- Chat API: 2
- Orders API: 1
- Health API: 1

## Tags
- Jira: @JIRA:LUJ-010 @JIRA:LUJ-011 @JIRA:LUJ-012
- Priority: @P1
- Requirements: @FR-API-001 @FR-API-002 @FR-API-003 @FR-API-004 @FR-API-005

## Initial Command (expected to fail)
- cd apps/middleware && mvn test -Dcucumber.filter.tags="@bdd"

