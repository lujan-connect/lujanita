en q# Git Commit Instructions - TravelCBooster

## Conventional Commits Format

All commits in this repository MUST follow the [Conventional Commits](https://www.conventionalcommits.org/) specification with Jira ticket integration.

## Commit Message Format

```
[JIRA-ID] type(scope): subject

[optional body]

[optional footer(s)]
```

### Structure Breakdown

1. **[JIRA-ID]**: Jira ticket identifier (e.g., TRAV-123) - **MANDATORY**
2. **type**: Type of change - **MANDATORY**
3. **(scope)**: Area affected - **OPTIONAL**
4. **subject**: Short description (imperative mood) - **MANDATORY**
5. **body**: Detailed explanation - **OPTIONAL**
6. **footer**: Breaking changes, references - **OPTIONAL**

---

## Automatic Jira ID Extraction

The Jira ticket ID is **automatically extracted from your branch name**:

### Branch Naming Convention

```bash
# Feature branches
feature/TRAV-123-short-description
feature/TRAV-456

# Bugfix branches
bugfix/TRAV-789-fix-description
fix/TRAV-101

# Hotfix branches
hotfix/TRAV-202-critical-fix

# Chore branches
chore/TRAV-303-update-deps
```

### Extraction Rules

- Pattern: `(TRAV-\d+)` anywhere in branch name
- Case insensitive (TRAV-123, trav-123 both work)
- First match is used if multiple IDs exist
- Format: Always uppercase in commit message (TRAV-XXX)

### Examples

| Branch Name | Extracted ID | Commit Starts With |
|-------------|--------------|-------------------|
| `feature/TRAV-1` | TRAV-1 | `[TRAV-1] feat:` |
| `feature/TRAV-123-automatic-responses` | TRAV-123 | `[TRAV-123] feat:` |
| `bugfix/TRAV-456` | TRAV-456 | `[TRAV-456] fix:` |
| `hotfix/trav-789-critical` | TRAV-789 | `[TRAV-789] fix:` |
| `main` or `devel` | *(none)* | `feat:` (no ID) |

---

## Commit Types

### Primary Types (Most Common)

| Type | Description | When to Use | Example |
|------|-------------|-------------|---------|
| **feat** | New feature | Adding new functionality | `[TRAV-1] feat(api): add email parsing endpoint` |
| **fix** | Bug fix | Fixing a defect | `[TRAV-2] fix(services): resolve correlation ID issue` |
| **docs** | Documentation | Only documentation changes | `[TRAV-3] docs(specs): update spec.md with edge cases` |
| **test** | Tests | Adding or updating tests | `[TRAV-4] test(api): add contract tests for EmailMessage` |
| **refactor** | Code refactoring | Code change that neither fixes bug nor adds feature | `[TRAV-5] refactor(packages): extract email-processor to package` |
| **chore** | Maintenance | Build process, dependencies, tooling | `[TRAV-6] chore(deps): upgrade FastAPI to 0.116.1` |

### Secondary Types (Less Common)

| Type | Description | Example |
|------|-------------|---------|
| **style** | Formatting, missing semicolons, etc. (no code change) | `[TRAV-7] style(api): format with black and prettier` |
| **perf** | Performance improvements | `[TRAV-8] perf(scrapper): optimize selector caching` |
| **build** | Build system or external dependencies | `[TRAV-9] build(turbo): update turborepo configuration` |
| **ci** | CI/CD configuration files and scripts | `[TRAV-10] ci(github): add PR validation workflow` |
| **revert** | Reverts a previous commit | `[TRAV-11] revert: revert "feat(api): add endpoint"` |

---

## Scope Guidelines

The scope specifies the area of the codebase affected. Use these standard scopes:

### App-Level Scopes

- `api` - Changes in `apps/api/`
- `services` - Changes in `apps/services/`
- `scrapper` - Changes in `apps/scrapper/`
- `dashboard` - Changes in `apps/dashboard/`
- `landing` - Changes in `apps/landing/`

### Package-Level Scopes

- `domain` - Changes in `packages/domain/`
- `ui` - Changes in `packages/ui/`
- `email-processor` - New package for email processing
- `ai-classifier` - New package for AI classification

### Cross-Cutting Scopes

- `monorepo` - Changes affecting entire monorepo
- `deps` - Dependency updates
- `config` - Configuration file changes
- `specs` - Specification document changes
- `docs` - Documentation changes

### Multiple Scopes

Use comma-separated list when affecting multiple areas:

```
[TRAV-15] feat(api,services): add webhook validation
[TRAV-16] refactor(domain,dashboard): update ServiceSubscription interface
```

---

## Subject Line Rules

1. **Imperative mood**: Use "add", not "added" or "adds"
2. **No capital first letter**: `add feature` not `Add feature`
3. **No period at end**: `add feature` not `add feature.`
4. **Max 72 characters** (including JIRA-ID, type, and scope)
5. **Be specific**: "add email parser" not "add stuff"

### ✅ Good Examples

```
[TRAV-1] feat(api): add email parsing endpoint
[TRAV-2] fix(services): resolve correlation ID propagation
[TRAV-3] docs(specs): update FR-001 with edge cases
[TRAV-4] test(api): add contract tests for EmailMessage schema
[TRAV-5] refactor(packages): extract email-processor to package
```

### ❌ Bad Examples

```
[TRAV-1] Added email parser                    # Wrong: past tense, no type/scope
[TRAV-2] fix(services): Fixes the bug.         # Wrong: capital letter, period
[TRAV-3] feat: stuff                           # Wrong: vague subject
[TRAV-4] Updated docs                          # Wrong: no type, no scope
TRAV-5 feat(api): add feature                  # Wrong: missing brackets []
```

---

## Body Guidelines

The body is **optional** but recommended for:
- Complex changes that need explanation
- Context about why the change was made
- Implementation decisions or trade-offs

### Body Format

- Separate from subject with blank line
- Wrap at 72 characters
- Use bullet points for multiple items
- Explain **what** and **why**, not **how** (code shows how)

### Example with Body

```
[TRAV-1] feat(api): add email parsing endpoint

Add POST /emails/parse endpoint to extract metadata from provider emails.

- Implements FR-001 from spec.md
- Uses Pydantic EmailMessage schema for validation
- Includes correlation ID for tracing
- Logs structured JSON for observability

This is foundational for automatic response processing feature.
```

---

## Footer Guidelines

Footers provide metadata about the commit:

### Breaking Changes

Use `BREAKING CHANGE:` footer for backwards-incompatible changes:

```
[TRAV-10] feat(api): update ServiceSubscription schema

BREAKING CHANGE: removed `serviceOptionKeys` field, replaced with `selectedOption`

Migration guide available in /docs/MIGRATIONS.md
```

### Issue References

Link to related issues or PRs:

```
[TRAV-5] fix(api): resolve authentication issue

Fixes #123
Closes #456
Refs #789
```

### Co-Authors

Credit multiple contributors:

```
[TRAV-8] feat(services): add AI classifier integration

Co-authored-by: Jane Doe <jane@example.com>
Co-authored-by: John Smith <john@example.com>
```

---

## Complete Examples

### Example 1: Simple Feature

```
[TRAV-1] feat(api): add email parsing endpoint
```

### Example 2: Bug Fix with Context

```
[TRAV-2] fix(services): resolve correlation ID propagation

Correlation IDs were not being passed between API and Services,
breaking distributed tracing. Now explicitly passed via headers.

Fixes #234
```

### Example 3: Breaking Change

```
[TRAV-10] feat(domain): update ServiceSubscription interface

Remove deprecated `serviceOptionKeys` array, replace with single
`selectedOption` field per subscription model changes.

BREAKING CHANGE: ServiceSubscription no longer has serviceOptionKeys.
Update all imports to use new selectedOption field.

Migration: packages/domain/MIGRATIONS.md
```

### Example 4: Multi-Scope Refactor

```
[TRAV-15] refactor(api,services,packages): extract email-processor package

Extract email processing logic from apps/api and apps/services
into reusable packages/email-processor package.

- Follows "Library & Package First" principle
- Enables code reuse across services
- Includes full test coverage
- Semantic versioning starts at 1.0.0

Refs #567
```

### Example 5: Test-First Implementation

```
[TRAV-20] test(api): add contract tests for EmailMessage schema

Implements Phase 2 test-first requirement from tasks.md.
Tests verify EmailMessage schema validation for:
- Required fields enforcement
- Email format validation
- Correlation ID format
- Timestamp parsing

Tests currently FAIL as expected (TDD principle).
Implementation will follow in next commit.
```

### Example 6: Documentation Update

```
[TRAV-25] docs(specs): complete plan.md for automatic responses

Add technical architecture decisions for email processing:
- LLM provider selection (Bedrock vs OpenAI)
- Email parsing library evaluation
- Package structure design
- Integration patterns with Travel Compositor

Completes Phase 0 prerequisites per Constitution.
```

---

## Branch-Specific Commit Conventions

### Feature Branches (`feature/TRAV-XXX`)

Primary types: `feat`, `test`, `docs`, `refactor`

```
[TRAV-1] feat(api): add email parsing endpoint
[TRAV-1] test(api): add tests for email parser
[TRAV-1] docs(specs): update spec.md with implementation notes
[TRAV-1] refactor(api): extract parser logic to helper
```

### Bugfix Branches (`bugfix/TRAV-XXX` or `fix/TRAV-XXX`)

Primary types: `fix`, `test`

```
[TRAV-50] fix(api): resolve authentication timeout
[TRAV-50] test(api): add regression test for timeout issue
```

### Hotfix Branches (`hotfix/TRAV-XXX`)

Primary types: `fix` (urgent production fixes)

```
[TRAV-99] fix(api): patch critical security vulnerability
```

### Chore Branches (`chore/TRAV-XXX`)

Primary types: `chore`, `build`, `ci`, `deps`

```
[TRAV-75] chore(deps): upgrade all dependencies
[TRAV-76] build(turbo): optimize build cache configuration
[TRAV-77] ci(github): add automated deployment workflow
```

---

## Special Cases

### No Jira Ticket (Exceptional)

For branches without Jira ID (e.g., `main`, `devel`, emergency fixes):

```
# Still use conventional commits, omit [JIRA-ID]
feat(api): add health check endpoint
fix(deps): resolve security vulnerability CVE-2024-XXXX
docs(readme): update setup instructions
```

**Note**: Most work should be tracked in Jira. Use this only for:
- Infrastructure/tooling setup
- Critical security patches
- Documentation fixes

### Multiple Tickets in One Commit

If a commit genuinely affects multiple tickets:

```
[TRAV-1,TRAV-2] feat(api,services): integrate email and AI services

Links email parsing (TRAV-1) with AI classification (TRAV-2)
for end-to-end automatic response processing.
```

**Warning**: This usually indicates the commit is too large. Consider splitting.

### Revert Commits

When reverting a previous commit:

```
[TRAV-30] revert: revert "[TRAV-28] feat(api): add experimental endpoint"

This reverts commit abc123def456.

Reason: Feature caused performance degradation in production.
Will reimplement with caching in next sprint.
```

---

## AI-Generated Commit Messages (Copilot/IDE)

This project uses **AI-powered commit message generation** from the IDE (GitHub Copilot, JetBrains AI, etc.).

### How AI Should Generate Messages

When Copilot or your IDE AI generates a commit message, it MUST:

1. **Extract Jira ID from current branch name**
   - Read current branch: `git symbolic-ref --short HEAD`
   - Extract pattern: `TRAV-\d+` (case insensitive)
   - Convert to uppercase: `TRAV-123`

2. **Analyze staged changes** to determine:
   - Type (feat, fix, docs, test, refactor, chore, etc.)
   - Scope (api, services, dashboard, domain, etc.)
   - Concise subject describing the change

3. **Generate message in format**: `[JIRA-ID] type(scope): subject`

4. **Add body if needed** for complex changes

### Example AI Generation Flow

```
Current branch: feature/TRAV-123-email-parsing
Staged files: apps/api/app/routers/emails.py, apps/api/tests/test_emails.py

AI Analysis:
- Jira ID: TRAV-123
- Type: feat (new functionality)
- Scope: api (changes in apps/api)
- Subject: add email parsing endpoint

Generated message:
[TRAV-123] feat(api): add email parsing endpoint
```

### Optional: Validation with commitlint

To validate messages after generation (optional):

```bash
# Install commitlint
npm install --save-dev @commitlint/{cli,config-conventional}

# Create .commitlintrc.json
{
  "extends": ["@commitlint/config-conventional"],
  "rules": {
    "subject-case": [2, "always", "lower-case"],
    "subject-full-stop": [2, "never", "."],
    "type-enum": [
      2,
      "always",
      ["feat", "fix", "docs", "style", "refactor", "test", "chore", "perf", "build", "ci", "revert"]
    ]
  }
}
```

Validation can be run manually: `npx commitlint --edit .git/COMMIT_EDITMSG`

---

## IDE Integration with AI Commit Generation

### GitHub Copilot (VSCode, JetBrains, etc.)

Copilot will automatically generate commit messages following this format when you use the commit panel.

**Setup**:
1. Ensure this file (`.github/git-commit-instructions.md`) exists in your repo
2. Copilot reads it automatically for context
3. When staging files and committing, Copilot will suggest messages in the correct format

**What Copilot will do**:
- Read current branch name to extract `TRAV-XXX`
- Analyze staged changes to determine type and scope
- Generate message: `[TRAV-XXX] type(scope): subject`

### JetBrains AI Assistant (IntelliJ, WebStorm, PyCharm)

JetBrains AI Assistant can generate commit messages following these conventions.

**Enable in**:
1. Settings → Version Control → Commit
2. Enable "Generate commit message with AI"
3. AI will use this file as reference

**Alternatively, use manual template**:
```
[TRAV-XXX] type(scope): subject

Body with detailed explanation

Refs #issue
```

### Cursor IDE / Windsurf

These AI-first IDEs read `.github/` instructions automatically.

**Usage**:
1. Stage your changes
2. Click "Generate commit message" or use AI command
3. AI will format according to these rules
4. Review and adjust if needed

### Manual Git Alias (Fallback)

If you prefer CLI, add to `~/.gitconfig`:

```ini
[alias]
  # Commit with Jira ID from branch
  cj = "!f() { \
    BRANCH=$(git symbolic-ref --short HEAD); \
    JIRA=$(echo $BRANCH | grep -oiE 'TRAV-[0-9]+' | head -1 | tr '[:lower:]' '[:upper:]'); \
    if [ -n \"$JIRA\" ]; then \
      git commit -m \"[$JIRA] $1\"; \
    else \
      git commit -m \"$1\"; \
    fi; \
  }; f"
```

Usage:
```bash
git cj "feat(api): add email parsing endpoint"
# Commits: [TRAV-123] feat(api): add email parsing endpoint
```

---

## Validation Checklist

Before committing, verify:

- [ ] Commit message follows `[JIRA-ID] type(scope): subject` format
- [ ] JIRA ID matches current branch (or intentionally omitted)
- [ ] Type is one of the allowed types
- [ ] Subject is in imperative mood (lowercase, no period)
- [ ] Subject length ≤ 72 characters (total message)
- [ ] Body (if present) explains **why**, not **how**
- [ ] Breaking changes documented in footer with `BREAKING CHANGE:`
- [ ] Tests pass: `npm test && cd apps/api && PYTHONPATH=. pytest`
- [ ] Code formatted: `npm run format && npm run lint`

---

## Quick Reference Card

```
Format:  [JIRA-ID] type(scope): subject

Types:   feat fix docs test refactor chore style perf build ci revert

Scopes:  api services scrapper dashboard landing domain ui monorepo deps config

Rules:   - Subject: imperative, lowercase, no period, ≤72 chars
         - Body: optional, wrap at 72 chars, explain why
         - Footer: BREAKING CHANGE, Fixes #, Refs #, Co-authored-by

Example: [TRAV-1] feat(api): add email parsing endpoint
```

---

## Common Mistakes to Avoid

| ❌ Wrong | ✅ Correct | Reason |
|---------|----------|--------|
| `TRAV-1 feat: add feature` | `[TRAV-1] feat(api): add feature` | Missing brackets and scope |
| `[TRAV-1] Added feature` | `[TRAV-1] feat(api): add feature` | Missing type, past tense |
| `[TRAV-1] feat(api): Add feature.` | `[TRAV-1] feat(api): add feature` | Capital letter, period |
| `[trav-1] feat: stuff` | `[TRAV-1] feat(api): add specific feature` | Lowercase ID, vague |
| `[TRAV-1] feat: did things` | `[TRAV-1] feat(api): add email parser` | No scope, vague |
| Multi-line subject | Subject on one line only | Subject must be single line |

---

## FAQ

**Q: What if my commit touches multiple apps?**  
A: Use comma-separated scopes: `feat(api,services): add feature`

**Q: What if I forget to add the JIRA ID?**  
A: Use `git commit --amend` to edit the message before pushing.

**Q: Should every commit have a body?**  
A: No, body is optional. Use for complex changes that need explanation.

**Q: Can I use emoji in commits?**  
A: No, keep commits professional and parseable by tools.

**Q: What about merge commits?**  
A: Git auto-generates merge commit messages. You can edit to add JIRA ID if needed.

**Q: How do I reference multiple Jira tickets?**  
A: Use comma-separated: `[TRAV-1,TRAV-2]` or reference others in body/footer.

**Q: What if I'm working on `main` without a feature branch?**  
A: Use conventional commits without JIRA ID, but this should be rare.

---

## For AI Assistants: How to Generate Commit Messages

When asked to generate a commit message for TravelCBooster, follow this algorithm:

### Step 1: Extract Jira ID from Branch

```javascript
// Pseudo-code for AI to follow
const branchName = getCurrentBranch(); // e.g., "feature/TRAV-123-email-parser"
const jiraIdMatch = branchName.match(/TRAV-\d+/i);
const jiraId = jiraIdMatch ? jiraIdMatch[0].toUpperCase() : null;

// Examples:
// "feature/TRAV-1" → "TRAV-1"
// "bugfix/TRAV-456-fix-bug" → "TRAV-456"
// "hotfix/trav-789" → "TRAV-789"
// "main" → null
```

### Step 2: Analyze Staged Changes

Determine:
- **Type**: What kind of change? (feat, fix, docs, test, refactor, chore, etc.)
- **Scope**: Which area? (api, services, dashboard, domain, ui, etc.)
- **Subject**: What specifically changed? (imperative mood, lowercase, no period)

```javascript
// Analysis logic
const stagedFiles = getStagedFiles();

// Determine type
const type = inferType(stagedFiles); // "feat", "fix", "test", etc.

// Determine scope
const scope = inferScope(stagedFiles); // "api", "services", etc.

// Generate subject
const subject = inferSubject(stagedFiles, diffContent);
```

### Step 3: Generate Message

```javascript
// Format message
let message = "";

if (jiraId) {
  message = `[${jiraId}] ${type}`;
} else {
  message = type;
}

if (scope) {
  message += `(${scope})`;
}

message += `: ${subject}`;

// Optional: Add body for complex changes
if (isComplexChange(stagedFiles)) {
  message += "\n\n" + generateBody(stagedFiles);
}
```

### Step 4: Examples by Context

#### Example 1: Feature in API
```
Branch: feature/TRAV-1-email-parser
Files: apps/api/app/routers/emails.py, apps/api/tests/test_emails.py
Changes: New endpoint for email parsing

Generated:
[TRAV-1] feat(api): add email parsing endpoint
```

#### Example 2: Bug Fix in Services
```
Branch: bugfix/TRAV-50-auth-timeout
Files: apps/services/app/auth.py
Changes: Fix timeout handling

Generated:
[TRAV-50] fix(services): resolve authentication timeout issue
```

#### Example 3: Tests Only
```
Branch: feature/TRAV-20-email-tests
Files: apps/api/tests/test_emails.py
Changes: Add contract tests

Generated:
[TRAV-20] test(api): add contract tests for EmailMessage schema
```

#### Example 4: Documentation
```
Branch: chore/TRAV-75-update-docs
Files: docs/API_STYLE_GUIDE.md, README.md
Changes: Update API documentation

Generated:
[TRAV-75] docs(api): update API style guide with new patterns
```

#### Example 5: Multiple Scopes
```
Branch: feature/TRAV-15-integration
Files: apps/api/app/routers/webhooks.py, apps/services/app/handlers.py
Changes: Integrate webhook handling

Generated:
[TRAV-15] feat(api,services): add webhook validation and handling
```

### Step 5: Validation Rules

Before presenting the message, validate:

```javascript
// Validation checklist
const isValid = 
  // Has Jira ID (unless on main/develop)
  (jiraId || ["main", "master", "develop", "devel"].includes(branchName)) &&
  
  // Type is valid
  ["feat", "fix", "docs", "test", "refactor", "chore", "style", "perf", "build", "ci", "revert"].includes(type) &&
  
  // Subject is lowercase
  subject === subject.toLowerCase() &&
  
  // Subject doesn't end with period
  !subject.endsWith(".") &&
  
  // Total length reasonable
  message.split("\n")[0].length <= 72;
```

### Decision Tree for Type Selection

```
Are tests the only changes?
├─ Yes → "test"
└─ No
   ├─ Is it documentation only?
   │  ├─ Yes → "docs"
   │  └─ No
   │     ├─ Does it fix a bug?
   │     │  ├─ Yes → "fix"
   │     │  └─ No
   │     │     ├─ Does it add new functionality?
   │     │     │  ├─ Yes → "feat"
   │     │     │  └─ No
   │     │     │     ├─ Is it code restructuring without behavior change?
   │     │     │     │  ├─ Yes → "refactor"
   │     │     │     │  └─ No
   │     │     │     │     ├─ Is it dependencies/tooling/build?
   │     │     │     │     │  ├─ Yes → "chore"
   │     │     │     │     │  └─ No → "style" or "perf"
```

### Scope Selection Logic

```javascript
// Map file paths to scopes
const scopeMapping = {
  "apps/api/": "api",
  "apps/services/": "services",
  "apps/scrapper/": "scrapper",
  "apps/dashboard/": "dashboard",
  "apps/landing/": "landing",
  "packages/domain/": "domain",
  "packages/ui/": "ui",
  "docs/": "docs",
  "specs/": "specs",
  "package.json": "deps",
  "requirements.txt": "deps",
  "turbo.json": "monorepo",
  ".github/": "ci"
};

// If multiple scopes, join with comma
const scopes = [...new Set(stagedFiles.map(file => 
  Object.entries(scopeMapping)
    .find(([path, _]) => file.startsWith(path))?.[1]
))].filter(Boolean);

const scope = scopes.length > 0 ? scopes.join(",") : null;
```

---

## Resources

- [Conventional Commits Specification](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/)
- [Commitlint Documentation](https://commitlint.js.org/)
- [Jira Smart Commits](https://support.atlassian.com/jira-software-cloud/docs/process-issues-with-smart-commits/)

---

**Last Updated**: 2025-10-30  
**Version**: 1.0.0  
**Maintained by**: TravelCBooster Dev Team

