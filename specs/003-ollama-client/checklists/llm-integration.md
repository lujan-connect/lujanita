# Checklist de Calidad de Requisitos: Integración LLM/Ollama

**Propósito:** Validar la calidad, claridad y completitud de los requisitos para la integración LLM/Ollama en el BFF,
asegurando cumplimiento SDD y mejores prácticas de prompts conversacionales.

---

## Requirement Completeness

- [ ] CHK001 ¿Están documentados todos los requisitos funcionales para la integración LLM/Ollama, incluyendo manejo de
  saludos, respuestas de bienvenida y formato de salida? [Spec §FR-LLM, plan.md]
- [ ] CHK002 ¿Se especifica cómo se debe construir el prompt de sistema y el mensaje de usuario para cada
  interacción? [Spec §FR-LLM, plan.md]
- [ ] CHK003 ¿Se define el comportamiento esperado ante fallos de Spring AI y fallback HTTP? [Spec §FR-LLM, plan.md]

## Requirement Clarity

- [ ] CHK004 ¿Las instrucciones para el modelo están redactadas de forma clara, sin ambigüedades ni
  repeticiones? [Spec §FR-LLM, assistantGuidelines]
- [ ] CHK005 ¿El formato de mensajes (system/user) está descrito explícitamente para Spring AI y fallback? [plan.md]
- [ ] CHK006 ¿Se especifica claramente la respuesta esperada ante un saludo simple ("hola")? [Spec §FR-LLM]

## Requirement Consistency

- [ ] CHK007 ¿El formato de respuesta JSON es consistente en todos los escenarios (éxito, error,
  fallback)? [Spec §FR-LLM, plan.md]
- [ ] CHK008 ¿Las instrucciones de contexto y directrices no se contradicen entre sí? [assistantGuidelines, plan.md]

## Acceptance Criteria Quality

- [ ] CHK009 ¿Los criterios de éxito para saludos, respuestas de bienvenida y manejo de errores son medibles y
  objetivos? [Spec §FR-LLM, tasks.md]

## Scenario & Edge Case Coverage

- [ ] CHK010 ¿Se cubren escenarios de saludo, consulta estándar, error de modelo y fallback
  HTTP? [Spec §FR-LLM, tasks.md]
- [ ] CHK011 ¿Se consideran edge cases como mensajes vacíos, saludos ambiguos o respuestas vacías del modelo? [tasks.md]

## Non-Functional Requirements

- [ ] CHK012 ¿Se especifican requisitos de latencia, robustez ante caídas y logging para la integración
  LLM? [plan.md, tasks.md]

## Dependencies & Assumptions

- [ ] CHK013 ¿Están documentadas las dependencias con Spring AI, Ollama y configuración YAML? [plan.md]
- [ ] CHK014 ¿Se explicitan supuestos sobre el comportamiento del modelo y la estructura de prompts? [plan.md, spec.md]

## Ambiguities & Conflicts

- [ ] CHK015 ¿Se identifican y resuelven posibles ambigüedades en la interpretación de saludos o formato de
  respuesta? [Spec §FR-LLM, assistantGuidelines]

---

**Meta:**

- Generado: 2025-11-29
- Basado en speckit.checklist.prompt.md y SDD Lujanita
- Cada ítem debe ser revisado antes de cambios en la integración LLM/Ollama

