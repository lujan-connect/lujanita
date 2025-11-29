# Quick Start Guide - Custom Agents

**For**: New developers and AI assistants  
**Time to Read**: 5 minutes  
**Purpose**: Get started with custom agents in TravelCBooster

---

## 🚀 What You Need to Know

### 1. Custom Agents Are Specialists

Think of them as expert colleagues who:
- Know specific parts of the codebase deeply
- Follow established patterns automatically
- Always work test-first
- Never break existing functionality

### 2. Six Main Agents

| Agent | Use For | Example |
|-------|---------|---------|
| `@sdd-workflow-specialist` | Starting new features | "Create spec for user auth" |
| `@data-model-specialist` | Defining entities | "Create Booking entity" |
| `@backend-api-specialist` | Building APIs | "Create POST /bookings" |
| `@frontend-ui-specialist` | Building UI | "Create BookingCard component" |
| `@testing-specialist` | Writing tests | "Test booking validation" |
| `@email-processing-specialist` | Email features | "Parse provider confirmation" |

### 3. Always Follows SDD Workflow

```
Spec First → Plan → Test → Implement → Validate
```

No shortcuts. No skipping steps. Always correct.

---

## 💡 Common Scenarios

### Scenario 1: I need a new API endpoint

```markdown
@backend-api-specialist Create POST /bookings endpoint that:
- Accepts userId, serviceId, checkIn, checkOut
- Validates dates (checkOut > checkIn)
- Returns 201 with booking ID
- Handles errors with proper codes
- Spec: /specs/002-booking-system/spec.md
```

**What Happens**:
1. Agent reads the spec
2. Agent reads backend-api-patterns.md
3. Agent writes FAILING tests
4. You run tests → They FAIL ✅
5. Agent creates schema + router
6. You run tests → They PASS ✅
7. Done!

### Scenario 2: I need a React component

```markdown
@frontend-ui-specialist Create BookingList component that:
- Displays list of bookings
- Shows status badges
- Supports English/Spanish
- Uses design system from packages/ui
- Spec: /specs/002-booking-system/spec.md
```

**What Happens**:
1. Agent reads the spec
2. Agent reads domain-entities.md
3. Agent creates component with proper types
4. Agent adds i18n support
5. Agent writes component tests
6. Done!

### Scenario 3: I'm starting a completely new feature

```markdown
@sdd-workflow-specialist I need to add payment processing:
- Credit card payments
- PayPal integration
- Payment history
- Refunds
- Guide me through the process
```

**What Happens**:
1. Agent creates /specs/003-payment/spec.md
2. Agent asks clarifying questions (max 3)
3. You answer questions
4. Agent completes spec with requirements
5. Agent creates data-model.md
6. Agent creates tasks.md
7. Ready for implementation!

### Scenario 4: I need to define a data entity

```markdown
@data-model-specialist Create Payment entity with:
- id, userId, bookingId
- amount, currency
- status (pending, completed, failed, refunded)
- paymentMethod (card, paypal)
- Create both TypeScript and Python versions
```

**What Happens**:
1. Agent creates TypeScript interface in packages/domain
2. Agent creates Pydantic schema in apps/api/schemas
3. Agent ensures field names are compatible (camelCase ↔ snake_case)
4. Agent adds validation rules
5. Agent exports properly
6. Done!

### Scenario 5: I need tests for existing code

```markdown
@testing-specialist Write tests for POST /bookings endpoint:
- Test successful creation
- Test invalid dates (checkOut before checkIn)
- Test missing required fields
- Test duplicate booking prevention
- Test unauthorized access
```

**What Happens**:
1. Agent reads existing code
2. Agent reads testing-guide.md
3. Agent creates comprehensive test suite
4. Agent includes edge cases
5. Agent adds fixtures if needed
6. You run tests → They should PASS
7. Done!

---

## 🎯 Invoking Patterns

### Basic Pattern

```markdown
@agent-name [Clear instruction]

Context:
- Spec: [path to spec]
- Phase: [Specification|Planning|Implementation|Testing]
- Expected: [what should happen]
```

### With Context

```markdown
@backend-api-specialist Create GET /bookings/{id} endpoint

Context:
- Spec: /specs/002-booking-system/spec.md
- Requirement: FR-003 (View booking details)
- Phase: Implementation
- Schema already exists: apps/api/schemas/booking.py
- Expected: Return booking or 404 with error code
```

### Multiple Agents

```markdown
First:
@data-model-specialist Define Booking entity

Then:
@testing-specialist Write tests for Booking CRUD

Finally:
@backend-api-specialist Implement Booking endpoints
```

---

## ⚡ Pro Tips

### 1. Always Provide the Spec

```markdown
✅ Good:
@backend-api-specialist Create endpoint following /specs/002-booking/spec.md FR-003

❌ Bad:
@backend-api-specialist Create an endpoint
```

### 2. Be Specific About Location

```markdown
✅ Good:
Create BookingCard in apps/dashboard/src/components/bookings/

❌ Bad:
Create BookingCard somewhere
```

### 3. Reference Requirements

```markdown
✅ Good:
Implement FR-001, FR-002, FR-003 from spec.md

❌ Bad:
Implement booking feature
```

### 4. Expect Test-First

```markdown
✅ Good:
Step 1: Write failing test
Step 2: Implement
Step 3: Verify test passes

❌ Bad:
Just implement it
```

### 5. One Agent at a Time

```markdown
✅ Good:
@testing-specialist Write tests
[Wait for completion]
@backend-api-specialist Implement feature

❌ Bad:
@testing-specialist @backend-api-specialist Do everything
```

---

## 🔍 Debugging Agent Behavior

### Problem: Agent Skips Tests

**Solution**: Invoke testing specialist first
```markdown
@testing-specialist Write tests for [feature] first
[Wait for tests]
@backend-api-specialist Now implement to make tests pass
```

### Problem: Agent Ignores Patterns

**Solution**: Explicitly reference knowledge base
```markdown
@backend-api-specialist Create endpoint following the pattern in:
.github/copilot-knowledge/backend-api-patterns.md
```

### Problem: Agent Modifies Wrong Files

**Solution**: Be explicit about scope
```markdown
@backend-api-specialist Create router

Constraints:
- ONLY create new file: apps/api/app/routers/bookings.py
- DO NOT modify other routers
- DO NOT modify main.py yet
```

### Problem: Agent Doesn't Follow Spec

**Solution**: Reference specific requirements
```markdown
@backend-api-specialist Implement these exact requirements:
- FR-001: Accept booking details
- FR-002: Validate dates
- FR-003: Return 201 status
From: /specs/002-booking/spec.md
```

---

## 📋 Verification Checklist

After agent completes work, verify:

- [ ] Tests exist and pass
- [ ] Follows pattern from knowledge base
- [ ] Includes observability (logs, errors)
- [ ] Documentation updated (if needed)
- [ ] Type checking passes
- [ ] Linter passes
- [ ] Spec requirements met

---

## 🎓 Learning Path

### Week 1: Read Documentation
1. Read [myagents.md](travelcbboter.agent.md) - Understand all agents
2. Read [.github/copilot-instructions.md](../copilot-instructions.md) - SDD workflow
3. Read [.github/copilot-knowledge/](../copilot-knowledge/) - Technical patterns

### Week 2: Watch Agents Work
1. Start with simple request: "Create GET /health endpoint"
2. Use `@backend-api-specialist`
3. Observe the workflow
4. Note how test-first works

### Week 3: Use All Agents
1. Start new feature with `@sdd-workflow-specialist`
2. Define entities with `@data-model-specialist`
3. Implement with `@backend-api-specialist` and `@frontend-ui-specialist`
4. Test with `@testing-specialist`

### Week 4: Create Custom Agent
1. Identify a domain needing specialized agent
2. Follow template in [myagents.md](travelcbboter.agent.md#-creating-your-own-custom-agent)
3. Define knowledge sources
4. Define constraints
5. Test with real scenarios

---

## 📚 Quick Reference Links

### Essential Reading
- **All Agents**: [myagents.md](travelcbboter.agent.md)
- **SDD Workflow**: [../copilot-instructions.md](../copilot-instructions.md)
- **Technical Patterns**: [../copilot-knowledge/](../copilot-knowledge/)

### Visual Guides
- **Workflow Diagrams**: [workflow-diagram.md](workflow-diagram.md)
- **Architecture Diagrams**: [/docs/diagrams/](../../docs/diagrams/)

### Examples
- **Backend Implementation**: [backend-api-specialist.example.md](backend-api-specialist.example.md)
- **Existing Features**: [/specs/001-*/](../../specs/)

---

## 🚨 Common Mistakes to Avoid

### ❌ Don't Skip Spec Phase

```markdown
Wrong:
"@backend-api-specialist Create booking system"
(No spec exists!)

Right:
"@sdd-workflow-specialist Create spec for booking system"
[Wait for spec]
"@backend-api-specialist Implement per spec"
```

### ❌ Don't Implement Before Tests

```markdown
Wrong:
"@backend-api-specialist Create endpoint and tests"
(Implementation comes first!)

Right:
"@testing-specialist Write tests for endpoint"
[Wait for tests]
"@backend-api-specialist Implement to pass tests"
```

### ❌ Don't Mix Agent Responsibilities

```markdown
Wrong:
"@backend-api-specialist Create endpoint and React component"
(Wrong domain!)

Right:
"@backend-api-specialist Create API endpoint"
"@frontend-ui-specialist Create React component"
```

### ❌ Don't Forget Context

```markdown
Wrong:
"@backend-api-specialist Create something"
(No context!)

Right:
"@backend-api-specialist Create POST /bookings
Spec: /specs/002-booking/spec.md
Requirement: FR-003"
```

---

## 💬 Getting Help

### Questions About Agents
- Read: [myagents.md](travelcbboter.agent.md)
- Check: [workflow-diagram.md](workflow-diagram.md)
- Review: [backend-api-specialist.example.md](backend-api-specialist.example.md)

### Questions About SDD
- Read: [../copilot-instructions.md](../copilot-instructions.md)
- Check: [/specs/001-*/](../../specs/) for examples
- Review: [../prompts/](../prompts/)

### Questions About Patterns
- Read: [../copilot-knowledge/](../copilot-knowledge/)
- Check: [/docs/](../../docs/)
- Review: Existing code in [apps/](../../apps/)

---

## 🎉 Success Stories

### Example 1: Email Processing Feature
```
Time: 2 days (vs 2 weeks manual)
Quality: 100% test coverage, zero bugs
Process:
1. @sdd-workflow-specialist created spec
2. @data-model-specialist defined entities
3. @email-processing-specialist implemented logic
4. @testing-specialist added integration tests
Result: Feature shipped on schedule, no issues
```

### Example 2: Booking System
```
Time: 3 days (vs 1 week manual)
Quality: Complete CRUD, full validation
Process:
1. @sdd-workflow-specialist created comprehensive spec
2. @data-model-specialist defined Booking entity
3. @backend-api-specialist created 5 endpoints
4. @frontend-ui-specialist created 3 components
5. @testing-specialist created 50+ tests
Result: Production-ready system with docs
```

---

## 🎯 Your First Task

**Try this now**:

```markdown
@sdd-workflow-specialist I want to add a health check endpoint to the API:
- Returns 200 OK when service is healthy
- Returns 503 if database unreachable
- Includes version info
- Create the specification for this
```

Follow the agent's guidance and see the SDD workflow in action!

---

**Version**: 1.0.0  
**Last Updated**: 2025-11-02  
**Next Steps**: Read [myagents.md](travelcbboter.agent.md) for complete documentation
