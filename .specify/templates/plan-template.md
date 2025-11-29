# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

[Extract from feature spec: primary requirement + technical approach from research]

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Python 3.11+ (FastAPI services), TypeScript (Next.js dashboard), Astro (landing)  
**Primary Dependencies**: FastAPI, SQLAlchemy, Pydantic, AWS SDK (Bedrock), OpenAI SDK, IMAP libraries  
**Storage**: PostgreSQL/RDS (metadata), S3 (email attachments), DynamoDB (optional for high-scale correlation)  
**Testing**: pytest (Python services), Jest (TypeScript), contract testing with JSON schemas  
**Target Platform**: Linux containers (Docker), AWS ECS/Lambda deployment  
**Project Type**: Microservices monorepo - apps/services for orchestration, packages for reusable components  
**Performance Goals**: 100+ emails/min processing, <5s LLM response time, 99.9% email parsing accuracy  
**Constraints**: <30s total processing per email, configurable LLM providers, audit trail compliance  
**Scale/Scope**: 1000+ emails/day, 50+ providers, integration with existing Travel Compositor system

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The following gates are derived from TravelCBooster's Spec-Driven Development
Constitution v1.0.0 and MUST be verified for TRAV-1 Epic implementation:

- **Tests-First (NON-NEGOTIABLE)**: Unit tests for email parsing logic, contract tests for EmailMessage/ResponseAnalysis/BookingUpdate schemas, integration tests for LLM providers and Travel Compositor API. All tests MUST be written first and fail before implementation.
- **Library & Package First**: Email processing, AI classification, and Travel Compositor integration MUST be implemented as reusable packages (packages/email-processor, packages/ai-classifier, packages/travel-compositor-client) with clear interfaces.
- **API/Contract-First**: JSON schemas for EmailMessage, ResponseAnalysis, and BookingUpdate MUST be defined before implementation. OpenAPI contracts for any new public endpoints in apps/api.
- **Observability & Error Handling**: Structured logging with correlation IDs across all email processing stages, metrics for LLM call success rates and processing times, audit trail for all Travel Compositor updates. Error handling for malformed emails, LLM timeouts, and API failures.
- **Versioning & Backward Compatibility**: Semantic versioning for all packages. LLM provider changes require MAJOR version bump if affecting public interfaces. Migration plan for any changes to existing Travel Compositor integration.

**Security Specific Gates**:
- No LLM API keys in code - use environment variables or AWS Secrets Manager
- Email content handling must comply with data privacy requirements
- Travel Compositor credentials secured and rotated
- Dependency scanning for all new LLM and email libraries

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (TravelCBooster Monorepo)

```text
# Microservices monorepo structure for TRAV-1 Epic implementation

apps/
├── services/                          # Main orchestration service (Email processing coordinator)
│   ├── app/
│   │   ├── routers/
│   │   │   └── email_processing.py   # New: TRAV-1 epic endpoints
│   │   ├── services/
│   │   │   └── email_orchestrator.py # New: Coordinates US1->US2->US3 workflow
│   │   └── settings.py               # Extended: Add LLM and email config
│   └── tests/
│       ├── contract/                 # Contract tests for email processing API
│       ├── integration/              # End-to-end workflow tests
│       └── unit/                     # Service layer unit tests
├── api/                              # Public API (if email status endpoints needed)
│   └── app/routers/
│       └── email_status.py           # New: Optional monitoring endpoints
└── dashboard/                        # Next.js admin interface
    └── src/
        └── pages/
            └── email-processing/     # New: Email processing monitoring UI

packages/
├── email-processor/                  # US1: TRAV-2 (Email reception & parsing)
│   ├── src/
│   │   ├── models/                  # EmailMessage, BookingCorrelation schemas
│   │   ├── connectors/              # IMAP/SMTP connectors
│   │   ├── parsers/                 # MIME parsing logic
│   │   ├── storage/                 # Metadata persistence
│   │   └── correlation/             # Email-booking mapping
│   └── tests/
├── ai-classifier/                   # US2: TRAV-3 (LLM analysis)
│   ├── src/
│   │   ├── models/                  # ResponseAnalysis schemas
│   │   ├── providers/               # Bedrock, OpenAI, Ollama
│   │   ├── prompts/                 # Classification prompts
│   │   ├── extractors/              # JSON schema extraction
│   │   └── feedback/                # Training loop
│   └── tests/
└── travel-compositor-client/        # US3: TRAV-4 (Travel Compositor integration)
    ├── src/
    │   ├── models/                  # BookingUpdate schemas
    │   ├── connectors/              # Travel Compositor API
    │   ├── validators/              # Booking difference validation
    │   ├── updaters/                # Status update logic
    │   └── processors/              # Email management
    └── tests/
```

**Structure Decision**: Microservices monorepo with reusable packages. The apps/services will orchestrate the workflow, while packages contain focused business logic that can be independently tested and versioned. This aligns with TravelCBooster's existing structure and the constitution's "Library & Package First" principle.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| Multiple LLM providers | Provider failover and cost optimization | Single provider creates vendor lock-in and single point of failure |
| 3 new packages | Separation of concerns for reusability | Monolithic approach violates "Library & Package First" principle |
| Email correlation logic | Automatic booking matching | Manual correlation doesn't scale with 1000+ emails/day |
