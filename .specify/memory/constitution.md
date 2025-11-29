# TravelCBooster Spec-Driven Development Constitution

<!--
Sync Impact Report

Version change: TEMPLATE -> 1.0.0

Modified principles:
- [PRINCIPLE_1_NAME] TEMPLATE -> "Library & Package First"
- [PRINCIPLE_2_NAME] TEMPLATE -> "Test-First (NON-NEGOTIABLE)"
- [PRINCIPLE_3_NAME] TEMPLATE -> "API / Contract-First"
- [PRINCIPLE_4_NAME] TEMPLATE -> "Observability & Error Handling"
- [PRINCIPLE_5_NAME] TEMPLATE -> "Versioning & Backward Compatibility"

Added sections:
- Security & Compliance
- Development Workflow (explicit CI/PR gates)

Removed sections: none

Templates requiring updates:
- .specify/templates/plan-template.md ✅ updated
- .specify/templates/spec-template.md ⚠ updated (clarified test/observability requirements)
- .specify/templates/tasks-template.md ✅ updated
- .specify/templates/checklist-template.md ⚠ pending (no structural conflicts but review recommended)

Follow-up TODOs:
- TODO(RATIFICATION_DATE): if this constitution is retroactively ratified, update the Ratified date.
-->

## Core Principles

### Library & Package First
Every new feature MUST prefer a reusable package or library when it produces shared
behavior or data models. Code that is intended to be consumed by multiple apps
MUST live in `packages/` (or a clearly named shared module). Libraries MUST be
self-contained, documented, and have their own test suite. Rationale: avoids
duplication, simplifies releases, and enables independent versioning.

### Test-First (NON-NEGOTIABLE)
All work MUST follow a test-first workflow. For each change, tests are written
before production code, and the tests MUST fail initially. Required test types
depend on scope but MUST include: unit tests for logic, contract tests for
public interfaces, and integration tests for cross-service behavior. Rationale:
prevents regressions and ensures deliverables are independently verifiable.

### API / Contract-First
Public service interfaces and shared schemas MUST be defined as explicit
contracts (OpenAPI for FastAPI services, TypeScript types for packages). Any
change to a contract MUST be accompanied by contract tests and a compatibility
statement (see Versioning). Rationale: clear expectations between teams and
automated verification of behavioral guarantees.

### Observability & Error Handling
Every service and library MUST emit structured logs (JSON), include meaningful
metrics, and map errors to documented error codes where appropriate. Traces or
correlation IDs should be propagated across service boundaries for important
user flows. Rationale: diagnosability in production and measurable reliability.

### Versioning & Backward Compatibility
Public packages and service APIs MUST use semantic versioning (MAJOR.MINOR.PATCH).
Breaking changes (MAJOR) require a documented migration plan and at least one
major bump review cycle. Non-breaking feature additions SHOULD use MINOR bumps
and bug fixes a PATCH bump. Rationale: predictable upgrades and safe rollouts.

## Security & Compliance
Secrets MUST be stored in environment variables or a secrets manager; no secrets
in source. Dependencies MUST be scanned for known CVEs during CI. Any
high-severity vulnerability found in a production dependency MUST be triaged
and patched or mitigated within the timeframe defined by team SLAs.

## Development Workflow
Code changes MUST be introduced via pull requests. Every PR MUST include:

- A short description of the change and its impact on public contracts.
- Links to failing tests (before implementation) and passing tests (after).
- A checklist showing compliance with constitution gates: tests, contract tests,
  observability, and a migration plan if applicable.

CI gates MUST run linting, unit tests, contract tests (where applicable), and a
basic security scan. No PR may be merged until CI is green and two reviewers,
including at least one maintainer familiar with the impacted area, approve.

## Governance
Amendments to this constitution are made by PR against
`.specify/memory/constitution.md`. An amendment is adopted when:

1. A PR includes the proposed changes, an explanation of the rationale, and a
	migration plan for any breaking changes.
2. CI passes for the proposed change.
3. The PR receives at least two approvals, one of which MUST be a maintainer
	responsible for an impacted domain (e.g., backend, frontend, infra).

Versioning policy for the constitution itself follows semantic versioning:

- MAJOR: backward-incompatible governance or removal/redefinition of
  non-negotiable principles.
- MINOR: addition of a principle or material expansion of guidance.
- PATCH: wording clarifications, typo fixes, or non-substantive refinements.

**Version**: 1.0.0 | **Ratified**: 2025-10-29 | **Last Amended**: 2025-10-29
