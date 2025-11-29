# Custom Agents for TravelCBooster - Spec-Driven Development

**Purpose**: Define custom AI agents specialized for specific domains in the TravelCBooster project  
**SDD Phase**: All Phases (Specification, Planning, Implementation, Testing)  
**Audience**: GitHub Copilot Workspace, AI Assistants, Development Team

---

## 📋 Overview

Custom agents are specialized AI assistants tuned for specific tasks within the Spec-Driven Development workflow. Each agent has deep knowledge of:
- Project domain and business logic
- Technical patterns and conventions
- Testing requirements
- SDD workflow phases

**Key Principle**: Custom agents follow the same SDD workflow as defined in `.github/copilot-instructions.md` but with domain-specific expertise.

---

## 🏗️ Custom Agent Architecture

### Agent Structure

Each custom agent definition includes:

```yaml
agent_name: descriptive-name
description: Brief description of agent purpose
expertise:
  - Domain area 1
  - Domain area 2
knowledge_base:
  - Path to knowledge files
  - Path to specs
  - Path to domain docs
phases:
  - Phases where agent is most useful
triggers:
  - Keywords or contexts that activate agent
constraints:
  - What agent should NOT do
examples:
  - Example usage scenarios
```

---

## 🤖 Example Custom Agents

### 1. Backend API Agent

**Agent Name**: `backend-api-specialist`

**Description**: Expert in FastAPI backend development, Pydantic schemas, and REST API patterns for TravelCBooster services.

**Expertise**:
- FastAPI endpoint creation and routing
- Pydantic v2 schemas with Firebase/Firestore integration
- API testing with pytest and TestClient
- RESTful design patterns
- Error handling and logging
- API documentation generation

**Knowledge Base**:
```
.github/copilot-knowledge/backend-api-patterns.md
.github/copilot-knowledge/domain-entities.md
.github/copilot-knowledge/testing-guide.md
docs/API_STYLE_GUIDE.md
apps/api/schemas/
apps/api/app/routers/
```

**SDD Phases**: Planning (Phase 1), Implementation (Phase 2), Testing (Phase 3)

**Triggers**:
- "Create API endpoint"
- "Add FastAPI router"
- "Implement REST API"
- "Add Pydantic schema"

**Constraints**:
- MUST follow test-first methodology
- MUST use Pydantic v2 BaseModel
- MUST include observability (logs, error codes)
- MUST NOT modify frontend code
- MUST NOT change database schema without data-model.md update

**Example Usage**:

```markdown
@backend-api-specialist Create a POST /bookings endpoint that:
- Accepts booking details (userId, serviceId, dates)
- Validates against existing schemas
- Returns 201 with created booking
- Handles duplicate booking errors
- Follows spec: /specs/002-booking-system/spec.md
```

**Expected Workflow**:
1. Read `/specs/002-booking-system/spec.md` for requirements
2. Read `backend-api-patterns.md` for FastAPI patterns
3. Read `domain-entities.md` for Booking entity
4. Write failing test in `apps/api/tests/test_bookings.py`
5. Suggest: "Run pytest - MUST FAIL"
6. Create schema in `apps/api/schemas/booking.py`
7. Create router in `apps/api/app/routers/bookings.py`
8. Register router in `apps/api/app/main.py`
9. Add observability (logs, error codes)
10. Suggest: "Run pytest - MUST PASS"

---

### 2. Frontend Components Agent

**Agent Name**: `frontend-ui-specialist`

**Description**: Expert in Next.js 15, React components, TypeScript, and TravelCBooster UI patterns.

**Expertise**:
- Next.js 15 App Router patterns
- React Server/Client Components
- TypeScript strict mode development
- Component library usage (`packages/ui`)
- Internationalization (i18n)
- Form handling and validation
- State management

**Knowledge Base**:
```
.github/copilot-knowledge/domain-entities.md
packages/ui/
packages/domain/
apps/dashboard/src/components/
docs/CONTRIBUTING.md
```

**SDD Phases**: Implementation (Phase 2), Testing (Phase 3)

**Triggers**:
- "Create React component"
- "Add Next.js page"
- "Implement UI feature"
- "Add form validation"

**Constraints**:
- MUST use TypeScript strict mode
- MUST use existing UI components from `packages/ui`
- MUST support i18n (English and Spanish)
- MUST write component tests
- MUST NOT hardcode user-facing text

**Example Usage**:

```markdown
@frontend-ui-specialist Create a BookingForm component that:
- Uses BookingRequest interface from packages/domain
- Includes date pickers for check-in/check-out
- Validates dates (check-out > check-in)
- Supports English and Spanish
- Follows spec: /specs/002-booking-system/spec.md
```

---

### 3. Data Model Agent

**Agent Name**: `data-model-specialist`

**Description**: Expert in domain entities, TypeScript interfaces, Pydantic schemas, and Firestore data modeling.

**Expertise**:
- Domain entity design
- TypeScript interface definition
- Pydantic schema creation
- Data relationships and constraints
- Firestore document structure
- Schema versioning and migration

**Knowledge Base**:
```
.github/copilot-knowledge/domain-entities.md
packages/domain/src/
apps/api/schemas/
docs/DOMAIN.md
```

**SDD Phases**: Specification (Phase 0), Planning (Phase 1)

**Triggers**:
- "Define entity"
- "Create data model"
- "Add domain type"
- "Update schema"

**Constraints**:
- MUST create both TypeScript and Python versions
- MUST maintain sync between frontend/backend types
- MUST document field aliases (camelCase ↔ snake_case)
- MUST include validation rules
- MUST NOT add breaking changes without version bump

**Example Usage**:

```markdown
@data-model-specialist Define a Booking entity with:
- id, userId, serviceId
- checkIn, checkOut dates
- status (pending, confirmed, cancelled)
- totalPrice
- Create TypeScript interface in packages/domain
- Create Pydantic schema in apps/api/schemas
```

---

### 4. Testing Agent

**Agent Name**: `testing-specialist`

**Description**: Expert in test-first development, pytest, Jest, and testing patterns for TravelCBooster.

**Expertise**:
- Test-first methodology (TDD)
- Pytest for Python backends
- Jest for TypeScript/React
- Test coverage analysis
- Mock and fixture creation
- Integration testing
- Contract testing

**Knowledge Base**:
```
.github/copilot-knowledge/testing-guide.md
apps/api/tests/
apps/dashboard/src/__tests__/
jest.config.js
```

**SDD Phases**: All phases (Test-First is mandatory)

**Triggers**:
- "Write tests"
- "Add test coverage"
- "Create fixtures"
- Before any implementation

**Constraints**:
- MUST write tests BEFORE implementation
- MUST ensure tests fail before implementation
- MUST achieve meaningful coverage
- MUST include edge cases
- MUST NOT mock unnecessarily

**Example Usage**:

```markdown
@testing-specialist Create tests for POST /bookings endpoint:
- Test successful booking creation
- Test validation errors (invalid dates, missing fields)
- Test duplicate booking prevention
- Test unauthorized access
- Create fixtures for test data
```

---

### 5. Email Processing Agent

**Agent Name**: `email-processing-specialist`

**Description**: Expert in email parsing, provider response handling, and integration with Travel Compositor (Feature 001).

**Expertise**:
- Email parsing and metadata extraction
- IMAP/Gmail API integration
- Provider response pattern recognition
- AI-powered classification
- Booking correlation logic
- Travel Compositor integration

**Knowledge Base**:
```
specs/001-automatic-provider-responses/
.github/copilot-knowledge/backend-api-patterns.md
docs/DOMAIN.md (Provider and Booking entities)
```

**SDD Phases**: Implementation (Phase 2), Testing (Phase 3)

**Triggers**:
- "Process provider email"
- "Parse booking confirmation"
- "Integrate with Travel Compositor"
- "Classify email response"

**Constraints**:
- MUST correlate emails with existing bookings
- MUST use OpenAI GPT-4 for classification
- MUST handle ambiguous responses (flag for manual review)
- MUST log all processing steps
- MUST NOT auto-confirm without validation

**Example Usage**:

```markdown
@email-processing-specialist Implement email correlation logic that:
- Extracts booking reference from email subject/body
- Queries Firestore for matching booking
- Returns booking with confidence score
- Handles multiple matches (ambiguous case)
- Logs correlation attempts
```

---

### 6. Spec-Driven Development Agent

**Agent Name**: `sdd-workflow-specialist`

**Description**: Expert in guiding developers through the SDD workflow, ensuring compliance with spec-first methodology.

**Expertise**:
- SDD workflow phases
- Specification creation and validation
- Plan generation
- Task breakdown
- Requirement traceability
- Documentation generation

**Knowledge Base**:
```
.github/copilot-instructions.md
.github/prompts/speckit.*.prompt.md
specs/
docs/CONTRIBUTING.md
```

**SDD Phases**: Phase 0 (Specification), guidance for all phases

**Triggers**:
- "Start new feature"
- "Create specification"
- "Generate tasks"
- "Plan implementation"

**Constraints**:
- MUST ensure spec exists before planning
- MUST validate spec completeness
- MUST ensure test-first approach
- MUST maintain traceability (requirements → tasks → code)
- MUST NOT allow implementation without specification

**Example Usage**:

```markdown
@sdd-workflow-specialist I need to add payment processing:
- Guide me through creating the spec
- Identify required entities
- Generate implementation plan
- Break down into testable tasks
```

---

## 🎯 Creating Your Own Custom Agent

### Step-by-Step Guide

#### 1. Define Agent Purpose

Ask yourself:
- What specific domain or task does this agent handle?
- What expertise does it need?
- Which SDD phase(s) does it support?

#### 2. Identify Knowledge Sources

List the files and directories the agent needs to read:
```
- Specification files (/specs/NNN-feature/)
- Knowledge base (.github/copilot-knowledge/)
- Code patterns (apps/, packages/)
- Documentation (docs/)
```

#### 3. Define Constraints

List what the agent MUST and MUST NOT do:
```
MUST:
- Follow test-first methodology
- Use existing patterns
- Include observability

MUST NOT:
- Break existing code
- Skip tests
- Ignore specifications
```

#### 4. Create Example Interactions

Write 2-3 example conversations showing:
- How to invoke the agent
- Expected workflow
- Output format

#### 5. Document in This File

Add your agent to the "Example Custom Agents" section above.

---

## 🔄 Agent Interaction Patterns

### Invoking a Custom Agent

```markdown
@agent-name [Instruction with context]

Context:
- Spec: /specs/NNN-feature/spec.md
- Current task: [specific task]
- Expected outcome: [what should happen]
```

### Agent Response Pattern

```markdown
## Agent: [agent-name]

### Understanding
[Restate the request and context]

### Approach
1. Read [relevant knowledge base files]
2. Review [relevant spec sections]
3. Follow [specific pattern]

### Test-First Implementation

**Step 1: Write Failing Test**
[Test code]

> Run: `pytest apps/api/tests/test_xyz.py -v`
> Expected: FAIL

**Step 2: Implementation**
[Implementation code]

> Run: `pytest apps/api/tests/test_xyz.py -v`
> Expected: PASS

### Observability
- Logs: [what gets logged]
- Errors: [error codes]
- Metrics: [what gets measured]

### Validation
[ ] Tests pass
[ ] Linter passes
[ ] Follows pattern
[ ] Spec requirements met
```

---

## 📝 Agent Template

Use this template to define new custom agents:

```markdown
### N. [Agent Name]

**Agent Name**: `agent-identifier`

**Description**: Brief description of agent purpose and expertise.

**Expertise**:
- Area 1
- Area 2
- Area 3

**Knowledge Base**:
```
path/to/knowledge1
path/to/knowledge2
```

**SDD Phases**: [Phases where agent is most useful]

**Triggers**:
- "Keyword 1"
- "Keyword 2"

**Constraints**:
- MUST [requirement 1]
- MUST [requirement 2]
- MUST NOT [restriction 1]
- MUST NOT [restriction 2]

**Example Usage**:

```markdown
@agent-identifier [Example request]
```

**Expected Workflow**:
1. Step 1
2. Step 2
3. ...
```

---

## 🔗 Integration with SDD Workflow

### Phase 0: Specification

**Active Agents**:
- `sdd-workflow-specialist` - Guides spec creation
- `data-model-specialist` - Defines entities

**Workflow**:
```
User: "I want to add feature X"
  ↓
@sdd-workflow-specialist: Creates /specs/NNN-feature/spec.md
  ↓
@data-model-specialist: Defines entities in data-model.md
  ↓
Spec ready for planning
```

### Phase 1: Planning

**Active Agents**:
- `sdd-workflow-specialist` - Creates plan.md
- `data-model-specialist` - Refines data model
- `backend-api-specialist` - Plans API contracts
- `frontend-ui-specialist` - Plans UI components

**Workflow**:
```
Read spec.md
  ↓
@sdd-workflow-specialist: Creates plan.md with architecture decisions
  ↓
@data-model-specialist: Creates detailed data-model.md
  ↓
Generate tasks.md with concrete implementation steps
```

### Phase 2: Implementation

**Active Agents**:
- `testing-specialist` - Creates tests first (TDD)
- `backend-api-specialist` - Implements API endpoints
- `frontend-ui-specialist` - Implements UI components
- `email-processing-specialist` - Implements domain logic

**Workflow**:
```
For each task in tasks.md:
  ↓
@testing-specialist: Write failing test
  ↓
Run test → MUST FAIL
  ↓
@[domain-specialist]: Implement feature
  ↓
Run test → MUST PASS
  ↓
Add observability (logs, errors, metrics)
  ↓
Next task
```

### Phase 3: Testing & Validation

**Active Agents**:
- `testing-specialist` - Adds integration tests
- `sdd-workflow-specialist` - Validates against spec

**Workflow**:
```
Unit tests pass
  ↓
@testing-specialist: Add integration tests
  ↓
@testing-specialist: Add contract tests
  ↓
@sdd-workflow-specialist: Validate all requirements met
  ↓
Feature complete
```

---

## 🎓 Best Practices

### 1. Single Responsibility

Each agent should focus on ONE domain or task type:
- ✅ Good: `backend-api-specialist` for FastAPI only
- ❌ Bad: `fullstack-specialist` for everything

### 2. Clear Constraints

Define what agent MUST and MUST NOT do:
```markdown
MUST:
- Follow existing patterns
- Write tests first
- Include documentation

MUST NOT:
- Modify unrelated code
- Skip validation
- Change architecture without approval
```

### 3. Knowledge Specificity

Point agents to specific knowledge sources:
- ✅ Good: `.github/copilot-knowledge/backend-api-patterns.md`
- ❌ Bad: "Read all documentation"

### 4. Test-First Always

Every agent implementing code MUST:
1. Write failing test
2. Verify it fails
3. Implement
4. Verify it passes

### 5. Observability Required

Every implementation MUST include:
- Structured logging
- Error codes
- Relevant metrics

---

## 🔍 Agent Debugging

### Common Issues

**Issue**: Agent ignores test-first methodology

**Solution**: Add explicit constraint:
```markdown
Constraints:
- MUST write test BEFORE implementation
- MUST verify test FAILS before coding
- MUST verify test PASSES after coding
```

**Issue**: Agent modifies unrelated code

**Solution**: Add clear scope constraint:
```markdown
Constraints:
- MUST ONLY modify files in [specific paths]
- MUST NOT change [specific areas]
```

**Issue**: Agent doesn't follow patterns

**Solution**: Add explicit knowledge base references:
```markdown
Knowledge Base:
```
.github/copilot-knowledge/[specific-pattern].md  ← READ THIS FIRST
```

---

## 📚 Related Documentation

- **SDD Workflow**: [`.github/copilot-instructions.md`](../copilot-instructions.md)
- **Knowledge Base**: [`.github/copilot-knowledge/README.md`](../copilot-knowledge/README.md)
- **Prompt Templates**: [`.github/prompts/`](../prompts/)
- **Feature Specs**: [`/specs/`](../../specs/)
- **API Style Guide**: [`/docs/API_STYLE_GUIDE.md`](../../docs/API_STYLE_GUIDE.md)

---

## 📊 Agent Effectiveness Metrics

Track these metrics to measure agent effectiveness:

### Code Quality
- Test coverage maintained/improved
- Linter pass rate
- Code review comment count

### SDD Compliance
- Specs created before code
- Test-first adherence
- Documentation completeness

### Efficiency
- Time to implement feature
- Bug count in implemented features
- Rework needed

---

## 🚀 Future Agent Ideas

Consider creating agents for:

1. **Performance Optimization Agent**
   - Analyzes slow endpoints
   - Suggests caching strategies
   - Optimizes database queries

2. **Security Agent**
   - Reviews authentication/authorization
   - Checks for common vulnerabilities
   - Validates input sanitization

3. **Migration Agent**
   - Helps upgrade dependencies
   - Handles breaking changes
   - Updates deprecated patterns

4. **Documentation Agent**
   - Generates API documentation
   - Updates README files
   - Creates architecture diagrams

5. **Monitoring Agent**
   - Adds observability to existing code
   - Creates dashboards
   - Sets up alerts

---

**Version**: 1.0.0  
**Last Updated**: 2025-11-02  
**Maintained By**: TravelCBooster Development Team  
**SDD Toolkit**: [GitHub Blog Article](https://github.blog/ai-and-ml/generative-ai/spec-driven-development-with-ai-get-started-with-a-new-open-source-toolkit/)
