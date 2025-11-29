# Custom Agents Directory

**Purpose**: Define specialized AI agents for the TravelCBooster Spec-Driven Development workflow  
**Audience**: AI Assistants, GitHub Copilot Workspace, Development Team

---

## 📁 Directory Structure

```
.github/agents/
├── README.md          # This file - overview and quick reference
└── myagents.md        # Custom agent definitions and templates
```

---

## 🤖 What Are Custom Agents?

Custom agents are specialized AI assistants with:
- **Deep domain knowledge** in specific areas (Backend, Frontend, Testing, etc.)
- **Access to relevant knowledge bases** (specs, patterns, documentation)
- **Defined constraints** (what they can and cannot do)
- **SDD workflow integration** (Phase 0-3 support)

---

## 🎯 Quick Reference

### When to Use Custom Agents

| Task | Agent | File Reference |
|------|-------|----------------|
| Create API endpoint | `@backend-api-specialist` | [myagents.md](travelcbboter.agent.md#1-backend-api-agent) |
| Build UI component | `@frontend-ui-specialist` | [myagents.md](travelcbboter.agent.md#2-frontend-components-agent) |
| Define data model | `@data-model-specialist` | [myagents.md](travelcbboter.agent.md#3-data-model-agent) |
| Write tests | `@testing-specialist` | [myagents.md](travelcbboter.agent.md#4-testing-agent) |
| Process emails | `@email-processing-specialist` | [myagents.md](travelcbboter.agent.md#5-email-processing-agent) |
| Start new feature | `@sdd-workflow-specialist` | [myagents.md](travelcbboter.agent.md#6-spec-driven-development-agent) |

### Agent Invocation Pattern

```markdown
@agent-name [Clear instruction with context]

Context:
- Spec: /specs/NNN-feature/spec.md
- Current Phase: [Specification|Planning|Implementation|Testing]
- Expected Outcome: [What should happen]
```

---

## 📋 Available Agents

### 1. Backend API Specialist
**Expertise**: FastAPI, Pydantic, REST APIs, pytest  
**Use For**: Creating endpoints, schemas, routers, API tests  
**SDD Phases**: Planning, Implementation, Testing

### 2. Frontend UI Specialist
**Expertise**: Next.js 15, React, TypeScript, i18n  
**Use For**: Components, pages, forms, UI tests  
**SDD Phases**: Implementation, Testing

### 3. Data Model Specialist
**Expertise**: Domain entities, TypeScript interfaces, Pydantic schemas  
**Use For**: Entity definitions, type creation, schema design  
**SDD Phases**: Specification, Planning

### 4. Testing Specialist
**Expertise**: TDD, pytest, Jest, test patterns  
**Use For**: Writing tests before implementation (Test-First)  
**SDD Phases**: All (Test-First is mandatory)

### 5. Email Processing Specialist
**Expertise**: Email parsing, AI classification, provider integration  
**Use For**: Feature 001 - Email processing and booking correlation  
**SDD Phases**: Implementation, Testing

### 6. SDD Workflow Specialist
**Expertise**: Spec-first methodology, feature planning, task breakdown  
**Use For**: Starting new features, creating specs, generating plans  
**SDD Phases**: Specification (Phase 0), guidance for all phases

---

## 🏗️ Creating Custom Agents

See [myagents.md - Creating Your Own Custom Agent](travelcbboter.agent.md#-creating-your-own-custom-agent) for:
- Step-by-step guide
- Agent template
- Best practices
- Example patterns

---

## 🔄 Integration with SDD Workflow

### Phase 0: Specification
```
User request
  ↓
@sdd-workflow-specialist → Creates spec.md
  ↓
@data-model-specialist → Defines entities
  ↓
Spec ready for planning
```

### Phase 1: Planning
```
spec.md
  ↓
@sdd-workflow-specialist → Creates plan.md
  ↓
@[domain-specialists] → Define architecture
  ↓
tasks.md generated
```

### Phase 2: Implementation
```
For each task:
  @testing-specialist → Write failing test
  @[domain-specialist] → Implement feature
  Run test → PASS
```

### Phase 3: Testing & Validation
```
@testing-specialist → Integration tests
@sdd-workflow-specialist → Validate against spec
```

---

## 📖 Documentation Links

### Within This Repository
- **Main Agents Doc**: [myagents.md](travelcbboter.agent.md)
- **SDD Instructions**: [../.github/copilot-instructions.md](../copilot-instructions.md)
- **Knowledge Base**: [../.github/copilot-knowledge/](../copilot-knowledge/)
- **Spec Templates**: [../.github/prompts/](../prompts/)

### External Resources
- **GitHub SDD Toolkit**: [Blog Article](https://github.blog/ai-and-ml/generative-ai/spec-driven-development-with-ai-get-started-with-a-new-open-source-toolkit/)
- **Project README**: [../../README.md](../../README.md)

---

## ⚡ Quick Start Examples

### Example 1: Create New API Endpoint

```markdown
@backend-api-specialist Create POST /bookings endpoint:
- Accept userId, serviceId, checkIn, checkOut
- Return 201 with booking ID
- Validate dates (checkOut > checkIn)
- Handle duplicate bookings
- Follow spec: /specs/002-booking-system/spec.md

Context:
- Phase: Implementation
- Spec exists: Yes
- Data model defined: Yes
```

### Example 2: Build UI Component

```markdown
@frontend-ui-specialist Create BookingCard component:
- Display booking details (dates, service, price)
- Show status badge (pending/confirmed/cancelled)
- Support English and Spanish
- Use design system from packages/ui
- Follow spec: /specs/002-booking-system/spec.md

Context:
- Phase: Implementation
- Component location: apps/dashboard/src/components/bookings/
```

### Example 3: Start New Feature

```markdown
@sdd-workflow-specialist I need to add user authentication:
- Support email/password and OAuth2
- JWT tokens with refresh
- Role-based access control
- Guide me through creating the specification

Context:
- Phase: Specification (Phase 0)
- No spec exists yet
```

---

## 🎓 Best Practices

### 1. Always Provide Context
```markdown
✅ Good:
@backend-api-specialist Create endpoint following spec.md section 3.2

❌ Bad:
@backend-api-specialist Create an endpoint
```

### 2. Reference Specifications
```markdown
✅ Good:
Follow requirements FR-001 to FR-005 in /specs/001-feature/spec.md

❌ Bad:
Just create what seems right
```

### 3. Expect Test-First
```markdown
✅ Good:
Step 1: Write failing test
Step 2: Verify FAIL
Step 3: Implement
Step 4: Verify PASS

❌ Bad:
Implement then test later
```

### 4. Include Observability
```markdown
✅ Good:
Add logging, error codes, and metrics

❌ Bad:
Just make it work
```

---

## 🔍 Troubleshooting

### Agent Not Following Pattern

**Problem**: Agent ignores knowledge base  
**Solution**: Explicitly reference in prompt:
```markdown
@agent-name [...instruction...]
Read: .github/copilot-knowledge/[specific-pattern].md
```

### Agent Breaking Constraints

**Problem**: Agent modifies unrelated code  
**Solution**: Add explicit scope:
```markdown
@agent-name [...instruction...]
Constraints:
- ONLY modify apps/api/routers/bookings.py
- DO NOT touch other files
```

### Agent Skips Tests

**Problem**: Agent implements without tests  
**Solution**: Invoke testing specialist first:
```markdown
@testing-specialist Write tests for [feature]
[Wait for tests]
@backend-api-specialist Implement to make tests pass
```

---

## 📊 Measuring Success

Track these metrics:
- ✅ Tests written before implementation (100%)
- ✅ Specs exist before coding (100%)
- ✅ Linter passes (100%)
- ✅ Code follows patterns (>95%)
- ✅ Documentation updated (100%)

---

## 🚀 Future Enhancements

Planned custom agents:
1. **Performance Optimization Agent** - Optimize slow endpoints
2. **Security Agent** - Review auth/security patterns
3. **Migration Agent** - Handle dependency upgrades
4. **Documentation Agent** - Auto-generate API docs
5. **Monitoring Agent** - Add observability to existing code

See [myagents.md - Future Agent Ideas](travelcbboter.agent.md#-future-agent-ideas) for details.

---

**Version**: 1.0.0  
**Last Updated**: 2025-11-02  
**Maintained By**: TravelCBooster Development Team
