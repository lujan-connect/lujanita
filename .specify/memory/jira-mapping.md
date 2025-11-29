# Jira Stories Mapping for Spec-Driven Development

**Updated**: 2025-11-03  
**Specification**: SPEC-001 (AUTOMATION.MAILS.SUPPLIER.RESPONSES)  
**Version**: 2.1.0  
**Author**: F. Schiavo  
**Purpose**: Links TravelCBooster Jira issues with spec-driven development framework

## Epic Overview

**TRAV-1**: Registro Automático de Respuestas de Proveedores  
**Spec ID**: SPEC-001  
**Spec Name**: AUTOMATION.MAILS.SUPPLIER.RESPONSES  
**Description**: Automatizar el procesamiento de correos de proveedores para registrar automáticamente confirmaciones o incidencias en el sistema Travel Compositor.

**Multi-tenant Context**: Sistema soporta múltiples clientes con suscripciones independientes, cada una con configuración propia de SMTP/IMAP, umbrales de IA, y reglas de clasificación.

This epic represents a complete feature that can be implemented using the spec-driven development approach. Below is the mapping to our constitution principles:

## User Stories & Spec-Driven Development Mapping

### User Story 1 (P1) - Recepción y parsing de correos (TRAV-2)
**Jira**: TRAV-2  
**Description**: Detectar correos entrantes, extraer contenido y adjuntos.

**Constitution Alignment**:
- ✅ **Library & Package First**: Email parsing logic should be in a reusable package
- ✅ **Test-First**: Unit tests for MIME parsing, integration tests for email detection
- ✅ **API/Contract-First**: Define EmailMessage schema, EmailParser interface
- ✅ **Observability**: Log email processing events, metrics on parsed emails
- ✅ **Versioning**: EmailMessage schema changes require migration plans

**Subtasks**:
- TRAV-5: Crear monitoreo de múltiples bandejas de correo (IMAP) - una por suscripción activa (RF-001)
- TRAV-6: Implementar parser MIME para extraer asunto, cuerpo y adjuntos (RF-002)
- TRAV-7: Persistir metadatos del correo en Firestore + Google Cloud Storage con campo subscriptionId (RF-002, RF-012)
- TRAV-8: Diseñar esquema de correlación correo ↔ reserva (messageId, threadId, bookingId) (RF-003)

### User Story 2 (P2) - Análisis automático de respuesta (TRAV-3)
**Jira**: TRAV-3  
**Description**: Usar LLM para determinar si el correo confirma, rechaza o genera duda sobre la reserva.

**Constitution Alignment**:
- ✅ **Library & Package First**: LLM analysis logic in packages/ai or similar
- ✅ **Test-First**: Contract tests for LLM responses, unit tests for classification
- ✅ **API/Contract-First**: ResponseAnalysis schema with status enum (confirmed/rejected/doubt)
- ✅ **Observability**: Log LLM calls, response times, confidence scores
- ✅ **Versioning**: LLM provider changes are MAJOR if affecting public API

**Subtasks**:
- TRAV-9: Configurar agente OpenAI GPT-4 con configuración por suscripción (RF-004, RF-011)
- TRAV-10: Diseñar prompt base y esquema de inferencia
- TRAV-11: Implementar extractor de "confirmación, rechazo o duda" en JSON con umbrales configurables (schema contract) (RF-005, RF-011)
- TRAV-12: Agregar capa de feedback loop para entrenar el agente con ejemplos reales (RF-009)

### User Story 3 (P3) - Registro y gestión del resultado (TRAV-4)
**Jira**: TRAV-4  
**Description**: Actualizar Travel Compositor, mover correos procesados y dejar los dudosos para revisión.

**Constitution Alignment**:
- ✅ **Library & Package First**: Travel Compositor connector as reusable service
- ✅ **Test-First**: Integration tests with Travel Compositor, email movement tests
- ✅ **API/Contract-First**: TravelCompositor API contracts, BookingUpdate schema
- ✅ **Observability**: Audit logs for all booking updates, error tracking
- ✅ **Versioning**: Travel Compositor API changes require compatibility checks

**Subtasks**:
- TRAV-13: Crear conector con Travel Compositor vía integración directa con reservations.py (RF-006)
- TRAV-14: Implementar lógica de validación de diferencias (precio, tipo habitación, etc.) (RF-010)
- TRAV-15: Registrar notas internas y actualizar estado del ítem en Travel Compositor vía reservations.py (RF-006)
- TRAV-16: Mover correo a carpetas configurables por suscripción (procesadas/pendientes/errores/archivadas) (RF-007)
- TRAV-17: Generar logs de auditoría con correlation IDs y métricas Prometheus (RF-008)
- **TRAV-18 (NUEVO)**: Crear entidad SuscripcionServicio con configuración SMTP/IMAP, umbrales IA, y carpetas (RF-012, RF-013)

## Spec-Driven Development Implementation Plan

### Phase 1: Setup (Foundational Infrastructure)
Based on the constitution requirements and Jira tasks:

**Required Packages**:
- `packages/email-processor` - For TRAV-5, TRAV-6, TRAV-8 (US1)
- `packages/ai-classifier` - For TRAV-9, TRAV-10, TRAV-11 (US2)  
- `packages/travel-compositor-client` - For TRAV-13, TRAV-14 (US3)

**Shared Infrastructure**:
- Database schema for email metadata (TRAV-7)
- Observability setup (TRAV-17)
- Configuration management for LLM providers (TRAV-9)

### Phase 2: User Story Implementation Order

1. **US1 - Email Processing (TRAV-2)** → MVP: Can receive and parse emails
2. **US2 - AI Analysis (TRAV-3)** → Can classify email responses  
3. **US3 - Result Management (TRAV-4)** → Can update Travel Compositor

Each story follows the constitution test-first approach:
- Write contract tests first (must fail)
- Write integration tests (must fail)  
- Implement to make tests pass
- Add observability and error handling
- Document API changes and version impacts

### Constitution Gates Checklist

For each user story, verify:

- [ ] **Tests-First**: Unit, contract, and integration tests written and failing before implementation
- [ ] **Contract Definition**: Schemas defined for EmailMessage, ResponseAnalysis, BookingUpdate
- [ ] **Observability**: Structured logging, metrics, correlation IDs across services
- [ ] **Security**: No secrets in code, dependency scanning, secure Travel Compositor connection
- [ ] **Versioning**: Semantic versioning for packages, migration plans for breaking changes

### Package Structure Alignment

```text
packages/
├── email-processor/           # TRAV-2 user story
│   ├── src/
│   │   ├── parsers/          # TRAV-6: MIME parser
│   │   ├── storage/          # TRAV-7: Metadata persistence  
│   │   ├── correlation/      # TRAV-8: Email-booking mapping
│   │   └── connectors/       # TRAV-5: IMAP/SMTP
│   └── tests/
├── ai-classifier/             # TRAV-3 user story  
│   ├── src/
│   │   ├── providers/        # TRAV-9: LLM providers
│   │   ├── prompts/          # TRAV-10: Base prompts
│   │   ├── extractors/       # TRAV-11: JSON schema extraction
│   │   └── feedback/         # TRAV-12: Training loop
│   └── tests/
└── travel-compositor-client/  # TRAV-4 user story
    ├── src/
    │   ├── connectors/       # TRAV-13: API/scraping
    │   ├── validators/       # TRAV-14: Difference validation
    │   ├── updaters/         # TRAV-15: Status updates
    │   └── processors/       # TRAV-16: Email management
    └── tests/
```

### Applications Integration

The packages will be consumed by:
- `apps/services` - Main orchestration service
- `apps/api` - Public API endpoints if needed
- Future apps that need email processing or AI classification

## Next Steps

1. Create feature specification following `spec-template.md` for TRAV-1 epic
2. Generate implementation plan using `plan-template.md`
3. Break down into tasks using `tasks-template.md` with Jira issue references
4. Implement following test-first approach per constitution

## Jira Integration Points

Each spec document should reference:
- **Epic**: TRAV-1 in feature title
- **User Stories**: TRAV-2, TRAV-3, TRAV-4 as priorities P1, P2, P3
- **Tasks**: Individual TRAV-X issues as implementation tasks
- **Links**: Direct links to Jira issues in task descriptions

**Jira URL Pattern**: `https://faguero.atlassian.net/browse/TRAV-X`

This creates full traceability from spec-driven development artifacts back to original Jira planning.

---

## New Requirements (Version 2.1.0 - Multi-tenant Support)

### Additional Tasks for Multi-tenant Implementation

**TRAV-18**: Gestión de Suscripciones al Servicio (NEW)  
**Type**: Story  
**Priority**: P0 (Blocker for all other stories)  
**Description**: Implementar sistema de suscripciones que permita a múltiples clientes usar el servicio con configuraciones independientes.

**Acceptance Criteria**:
1. Crear entidad `SuscripcionServicio` con campos: clientId, serviceId, emailConfig, aiConfig, folderConfig, status
2. Implementar CRUD de suscripciones (crear, leer, actualizar, suspender, cancelar)
3. Almacenar credenciales SMTP/IMAP encriptadas por suscripción
4. Permitir configurar umbrales de confianza de IA por suscripción (default: 0.7)
5. Permitir configurar carpetas de destino por suscripción
6. Todas las entidades (MensajeCorreo, etc.) deben incluir `subscriptionId`

**Technical Details**:
- Schema: Ver `specs/001-*/data-model.md` sección "SuscripcionServicio"
- Storage: Firestore collection `service_subscriptions`
- Encryption: Google Cloud KMS para credenciales SMTP/IMAP
- RF Mapping: RF-011, RF-012, RF-013

**Subtasks**:
- TRAV-18.1: Definir schema SuscripcionServicio (TypeScript + Python Pydantic)
- TRAV-18.2: Crear migration para tabla/colección service_subscriptions
- TRAV-18.3: Implementar servicio de encriptación de credenciales (Google Cloud KMS)
- TRAV-18.4: Crear API endpoints para CRUD de suscripciones
- TRAV-18.5: Modificar entidades existentes para incluir subscriptionId
- TRAV-18.6: Implementar validación de suscripción activa antes de procesar correos
- TRAV-18.7: Tests de integración multi-tenant (múltiples suscripciones simultáneas)

---

## Updated Requirements Mapping

**New Functional Requirements** (added in v2.1.0):

- **RF-011**: Sistema DEBE permitir configurar umbrales de confianza de IA por suscripción (por defecto: 0.7)
  - Maps to: TRAV-9, TRAV-11, TRAV-18
  
- **RF-012**: Sistema DEBE soportar múltiples clientes (multi-tenant) con configuraciones de SMTP/IMAP independientes por suscripción
  - Maps to: TRAV-5, TRAV-7, TRAV-18
  
- **RF-013**: Sistema DEBE gestionar suscripciones al servicio con parámetros configurables (conexión IMAP/SMTP, umbrales, carpetas, reglas de clasificación)
  - Maps to: TRAV-18

**Updated Requirements**:

- **RF-001**: El sistema DEBE monitorear **múltiples** bandejas de correo, una por cada suscripción activa del servicio
  - Updated from: "una bandeja de correo designada"
  - Maps to: TRAV-5, TRAV-18
  
- **RF-006**: El sistema DEBE actualizar Travel Compositor vía **integración con reservations.py** cuando el análisis de IA sea concluyente
  - Updated from: "vía API o web scraping"
  - Maps to: TRAV-13, TRAV-15

**New Success Criteria**:

- **CE-008**: El sistema soporta múltiples suscripciones simultáneas (multi-tenant) con aislamiento completo de datos y configuraciones
  - Maps to: TRAV-18

---

## Migration Impact on Existing Tasks

### TRAV-2 (Email Processing) - Impact: HIGH
**Changes Required**:
- Add `subscriptionId` field to all email processing operations
- Load IMAP/SMTP config from subscription instead of environment variables
- Support monitoring multiple mailboxes concurrently (one per active subscription)

### TRAV-3 (AI Analysis) - Impact: MEDIUM
**Changes Required**:
- Load AI confidence threshold from subscription config (default: 0.7)
- Support per-subscription custom prompts if configured
- Pass subscriptionId through analysis pipeline

### TRAV-4 (Result Management) - Impact: MEDIUM
**Changes Required**:
- Use subscription's folder mapping config when moving emails
- Update Travel Compositor integration to use reservations.py instead of scraping
- Ensure audit logs include subscriptionId for traceability

---

## Implementation Priority (Updated)

**Phase 0: Multi-tenant Foundation** (BLOCKING)
1. TRAV-18: Subscription Management System
   - Required before any other work can proceed
   - Provides configuration backbone for all features

**Phase 1: Core Features** (ORIGINAL PLAN + UPDATES)
1. TRAV-2: Email Processing (with multi-tenant support)
2. TRAV-3: AI Analysis (with configurable thresholds)
3. TRAV-4: Result Management (with reservations.py integration)

**Phase 2: Enhancements**
1. TRAV-12: Feedback loop
2. Performance optimization for high-volume clients
3. Advanced subscription management UI

---

## Action Items for Jira Update

**To apply these changes in Jira**:

1. **Update TRAV-1 (Epic)**:
   - Add "Spec ID: SPEC-001" to description
   - Add "Spec Name: AUTOMATION.MAILS.SUPPLIER.RESPONSES" to description
   - Add "Multi-tenant Support: YES" label
   - Update version to 2.1.0

2. **Create TRAV-18 (New Story)**:
   - Title: "Gestión de Suscripciones al Servicio (Multi-tenant)"
   - Type: Story
   - Priority: P0 (Highest)
   - Link to Epic: TRAV-1
   - Add description from above
   - Create 7 subtasks (TRAV-18.1 to TRAV-18.7)

3. **Update TRAV-2** (Email Processing):
   - Add to description: "Support multiple mailboxes (one per subscription)"
   - Add RF-001, RF-012 to requirements section
   - Add dependency: Blocked by TRAV-18

4. **Update TRAV-3** (AI Analysis):
   - Add to description: "Configurable confidence thresholds per subscription (default: 0.7)"
   - Add RF-011 to requirements section
   - Add dependency: Blocked by TRAV-18

5. **Update TRAV-4** (Result Management):
   - Update description: Change "API REST o scraping" to "integración directa con reservations.py"
   - Add RF-006 update note
   - Add dependency: Blocked by TRAV-18

6. **Update TRAV-5**:
   - Change description to: "Monitorear múltiples bandejas IMAP (una por suscripción activa)"

7. **Update TRAV-7**:
   - Add to description: "Incluir campo subscriptionId en todos los registros"
   - Add storage note: "Firestore + Google Cloud Storage"

8. **Update TRAV-13**:
   - Change description from "API REST o scraping" to "Integración directa con reservations.py"
   - Add technical note: "Usar módulo reservations.py del servicio Travel Compositor"

9. **Update TRAV-17**:
   - Add to description: "Incluir subscriptionId en todos los logs para trazabilidad"
   - Add: "Usar correlation IDs en todos los logs estructurados"