# Knowledge Base Index

**Spec-Driven Development (SDD) Knowledge Base for Lujanita**

Este directorio contiene artefactos de conocimiento estructurado que GitHub Copilot y otros asistentes AI usan durante la generación de código. Estos documentos siguen las convenciones del [GitHub Spec-Driven Development toolkit](https://github.blog/ai-and-ml/generative-ai/spec-driven-development-with-ai-get-started-with-a-new-open-source-toolkit/).

---

## 📚 Artículos de Conocimiento Disponibles

### Middleware Java

| Artículo | Propósito | Usar Cuando |
|----------|-----------|-------------|
| **[backend-api-patterns.md](backend-api-patterns.md)** | Patrones Spring Boot, DTOs, cliente MCP, Ollama | Crear endpoints REST, integrar con Odoo MCP, invocar modelo local |

### Modelado de Datos

| Artículo | Propósito | Usar Cuando |
|----------|-----------|-------------|
| **[domain-entities.md](domain-entities.md)** | Esquemas de entidades (TypeScript + Java) | Trabajar con modelos de dominio, tipos, contratos MCP |

### Testing

| Artículo | Propósito | Usar Cuando |
|----------|-----------|-------------|
| **[testing-guide.md](testing-guide.md)** | Patrones test-first (BDD + Unit) | Escribir tests antes de implementar |

### Contratos MCP

| Artículo | Propósito | Usar Cuando |
|----------|-----------|-------------|
| **[contracts-mcp.md](contracts-mcp.md)** | Definiciones de contratos MCP con Odoo | Agregar nuevas operaciones MCP, validar payloads |

---

## 🔄 How AI Assistants Use This

### Reading Order

When generating code, AI assistants read:

1. **[`/specs/NNN-feature/`](../../specs/)** - Current feature specification
2. **[`../.github/copilot-instructions.md`](../copilot-instructions.md)** - Project-wide patterns and philosophy  
3. **This directory** - Specific technical patterns
4. **[`/docs/`](../../docs/)** - Reference documentation
   - **[`/docs/diagrams/README.md`](../../docs/diagrams/README.md)** - Architecture diagrams (visual context)

### Patrón de Uso

```
Usuario solicita: "Crear endpoint GET /orders/{orderId}"
  ↓
AI lee:
  1. /specs/002-order-tracking/spec.md (si existe)
  2. .github/copilot-instructions.md (contexto general)
  3. .github/copilot-knowledge/backend-api-patterns.md (cómo hacerlo)
  4. packages/contracts/ (contrato MCP para orders.get)
  4. .github/copilot-knowledge/domain-entities.md (data models)
  5. .github/copilot-knowledge/testing-guide.md (test-first)
  ↓
AI generates:
  1. Test (MUST FAIL first)
  2. Schema
  3. Router
  4. Registration
```

---

## 📖 Article Format

All articles in this directory follow this structure:

```markdown
# [Topic] - Knowledge Article

Brief description of what this covers.

---

## Pattern: [Pattern Name]

### When to Use
[Conditions]

### Implementation Steps
1. Step one
2. Step two
...

### Example
[Complete, runnable code]

### Common Mistakes
- Mistake 1
- Mistake 2

---
```

---

## 🎯 SDD Compliance

This knowledge base is **fully compliant** with GitHub's Spec-Driven Development toolkit:

✅ **Location**: `.github/copilot-knowledge/` (GitHub Copilot reads this directory)  
✅ **Format**: Structured patterns with step-by-step instructions  
✅ **Test-First**: All patterns enforce TDD (tests before implementation)  
✅ **Examples**: Complete, executable code samples  
✅ **Integration**: Links to `/specs/` and `copilot-instructions.md`

---

## 📝 Adding New Articles

To add a new knowledge article:

1. **Create file** in this directory: `.github/copilot-knowledge/[topic].md`
2. **Follow format** above
3. **Add to table** in this README
4. **Test with AI** - Verify Copilot can read and use it
5. **Link from copilot-instructions.md** if it's a major pattern

---

## 🔗 Related Resources

- **Project Instructions**: [`../copilot-instructions.md`](../copilot-instructions.md)
- **Feature Specs**: [`/specs/`](../../specs/)
- **Reference Docs**: [`/docs/`](../../docs/)
- **Commit Format**: [`../git-commit-instructions.md`](../git-commit-instructions.md)

---

**Last Updated**: 2025-10-30  
**SDD Toolkit**: [GitHub Blog Article](https://github.blog/ai-and-ml/generative-ai/spec-driven-development-with-ai-get-started-with-a-new-open-source-toolkit/)  
**Directory**: `.github/copilot-knowledge/`

