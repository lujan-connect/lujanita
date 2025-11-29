# ✅ Checklist Pre-Spec 1

## Estado del Proyecto Base

Este checklist debe completarse **ANTES** de comenzar con cualquier especificación (Spec 1).

---

## 📁 Estructura de Directorios

- [x] `/apps/middleware/` - Directorio del middleware Java
- [x] `/apps/widget/` - Directorio del widget React (a crear)
- [x] `/packages/contracts/` - Contratos MCP compartidos
- [x] `/platform/ollama/` - Configuración de Ollama
- [x] `/platform/odoo/` - Configuración de Odoo MCP (a crear)
- [x] `/specs/` - Especificaciones SDD
- [x] `/docs/architecture/` - Documentación de arquitectura
- [x] `/docs/deployment/` - Guías de deployment
- [x] `/.github/copilot-knowledge/` - Base de conocimiento para IA
- [x] `/.github/prompts/` - Prompts de Spec Kit

---

## 🛠️ Configuración del Middleware

- [x] `pom.xml` creado con todas las dependencias
  - [x] Spring Boot 3.2
  - [x] Java 21
  - [x] Ollama4J
  - [x] Cucumber
  - [x] JUnit 5
  - [x] REST Assured
  - [x] Logstash Logback

- [x] Maven Wrapper configurado (`.mvn/wrapper/`)
- [x] `.gitignore` para Java/Maven
- [x] `README.md` del middleware con:
  - [x] Estructura del proyecto
  - [x] Comandos principales
  - [x] Configuración
  - [x] Desarrollo con SDD

---

## 📚 Documentación

- [x] `.github/copilot-instructions.md` actualizado
  - [x] Contexto con Ollama embebido
  - [x] Comandos Maven (no Gradle)
  - [x] Referencias a Ollama

- [x] `.github/copilot-knowledge/contracts-mcp.md` actualizado
  - [x] Contratos con Odoo
  - [x] Contratos con Ollama
  - [x] Códigos de error para ambos
  - [x] Versionado con Maven

- [x] `docs/architecture/README.md` creado
  - [x] Visión general de la arquitectura
  - [x] Flujo de datos
  - [x] Decisiones de arquitectura (ADRs)
  - [x] Observabilidad
  - [x] Despliegue
  - [x] Seguridad
  - [x] Escalabilidad

- [x] `docs/deployment/ollama-setup.md` creado
  - [x] Requisitos de VM
  - [x] Instalación de Ollama
  - [x] Configuración como servicio
  - [x] Descarga de modelos
  - [x] Optimización de rendimiento
  - [x] Instalación del middleware
  - [x] Nginx como proxy
  - [x] Monitoreo y troubleshooting

- [x] `README.md` principal actualizado
  - [x] Arquitectura con Ollama
  - [x] Flujo SDD

---

## 🧪 Testing (Pendiente - Se creará con Spec 1)

- [ ] Estructura de tests BDD (`src/test/resources/features/`)
- [ ] Runners de Cucumber configurados
- [ ] Tests base que fallen (red phase)

---

## 🔧 Configuración Local (Pendiente - Opcional antes de Spec 1)

- [ ] Ollama instalado localmente
- [ ] Modelo `tinyllama` descargado
- [ ] Middleware compila correctamente (`mvn clean install`)
- [ ] Health check responde (cuando se implemente)

---

## 📋 Contratos MCP (Documentados, NO Implementados)

- [x] `orders.get` - Obtener orden desde Odoo
- [x] `customers.search` - Buscar clientes
- [x] `products.list` - Listar productos
- [x] `deliveries.track` - Rastrear entregas
- [x] `ollama.chat` - Chat con LLM
- [x] `ollama.embed` - Generar embeddings

**Nota:** Los contratos están **DOCUMENTADOS** pero **NO IMPLEMENTADOS**. Se implementarán según las especificaciones.

---

## 🚀 Siguiente Paso: Spec 1

Una vez completado este checklist, puedes proceder a crear la primera especificación:

### Opciones Sugeridas para Spec 1:

1. **Health Check & Observabilidad Básica**
   - Endpoint `/health` con checks de Ollama y Odoo
   - Logs estructurados
   - Métricas básicas
   - **Ventaja:** Base sólida para todo lo demás

2. **Integración con Ollama**
   - Servicio de chat básico
   - Manejo de errores
   - Timeouts y retries
   - **Ventaja:** Funcionalidad core del chatbot

3. **Integración con Odoo MCP - Orders**
   - Cliente MCP básico
   - Operación `orders.get`
   - Manejo de errores MCP
   - **Ventaja:** Integración con datos reales

### Proceso para Crear Spec 1:

```bash
# 1. Crear carpeta de especificación
mkdir -p specs/001-health-check

# 2. Usar prompt de especificación
# Ver: .github/prompts/speckit.specify.prompt.md

# 3. Generar spec.md con:
#    - User Stories
#    - Requisitos funcionales (FR-XXX)
#    - Criterios de aceptación

# 4. Generar plan.md con decisiones técnicas

# 5. Generar feature files Gherkin
# Ver: .github/prompts/speckit.gherkin.prompt.md

# 6. Implementar test-first siguiendo SDD
```

---

## ✅ Checklist Completo

**Estado actual:** COMPLETO ✅

### Verificación de Archivos .github

- [x] `.github/copilot-instructions.md` - Actualizado con Maven, Ollama, LUJ-XXX
- [x] `.github/git-commit-instructions.md` - Cambiado TRAV-XXX por LUJ-XXX
- [x] `.github/copilot-knowledge/*.md` - Actualizados con mvnw y contratos Ollama
- [x] Sin referencias a TravelCBooster en archivos críticos
- [x] Sin referencias a Python/FastAPI en archivos activos

**Ver detalles completos en**: `.github/VERIFICATION-REPORT.md`

### Listo para Spec 1

Puedes proceder a crear la **Spec 1**. Se recomienda comenzar con **Health Check & Observabilidad Básica** para tener una base sólida.

---

**Última actualización:** 2025-11-29  
**Próximo paso:** Crear `/specs/001-<feature>/spec.md`

