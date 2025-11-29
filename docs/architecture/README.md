# Arquitectura de Lujanita

## Visión General

Lujanita es un chatbot conversacional para Lujan de Cuyo Express que facilita consultas sobre órdenes, productos y entregas. La arquitectura sigue un diseño de tres capas:

```
┌──────────────────────────────────────────────────────────┐
│                    Widget React                          │
│  (Interfaz conversacional embebible en web/móvil)       │
└────────────────────┬─────────────────────────────────────┘
                     │ REST API
                     ▼
┌──────────────────────────────────────────────────────────┐
│              Middleware Java + Spring Boot               │
│  ┌────────────────┐           ┌────────────────┐        │
│  │  Cliente MCP   │◄─────────►│ Cliente Ollama │        │
│  └────────┬───────┘           └────────────────┘        │
└───────────┼──────────────────────────────────────────────┘
            │ MCP Protocol        │ Ollama API
            ▼                     ▼
┌─────────────────────┐   ┌──────────────────┐
│   Odoo + MCP Server │   │  Ollama Embebido │
│   (sale.order,      │   │  (tinyllama/phi2)│
│    res.partner,     │   │                  │
│    product.product) │   │                  │
└─────────────────────┘   └──────────────────┘
```

## Componentes

### 1. Widget React (`apps/widget/`)

**Responsabilidades:**
- Interfaz de usuario conversacional
- Internacionalización (ES/EN)
- Gestión de estado de conversación
- Renderizado de mensajes y respuestas

**Tecnologías:**
- React 18
- Vite
- TypeScript
- Vitest + Testing Library
- Cucumber (BDD)

**Comunicación:**
- REST API hacia el middleware
- WebSocket (futuro) para streaming de respuestas

### 2. Middleware Java (`apps/middleware/`)

**Responsabilidades:**
- Orquestación entre Odoo y Ollama
- Cliente MCP hacia Odoo
- Cliente Ollama para LLM
- Lógica de negocio y transformación de datos
- Autenticación y autorización (futuro)
- Observabilidad (logs, métricas, trazas)

**Tecnologías:**
- Java 21
- Spring Boot 3.2
- Maven
- Ollama4J
- WebClient (cliente HTTP reactivo)
- Cucumber + JUnit 5

**Patrones:**
- Clean Architecture
- Repository Pattern para abstraer MCP
- Circuit Breaker para resiliencia (futuro)
- Cache local para reducir latencia

### 3. Odoo + MCP Server (`platform/odoo/`)

**Responsabilidades:**
- Fuente de verdad para datos de negocio
- Exponer operaciones vía MCP
- Gestión de órdenes, clientes, productos

**Modelos Expuestos:**
- `sale.order` - Órdenes de venta
- `res.partner` - Clientes
- `product.product` - Productos
- `stock.picking` - Entregas

**Operaciones MCP:**
- `orders.get`
- `customers.search`
- `products.list`
- `deliveries.track`

### 4. Ollama Embebido (`platform/ollama/`)

**Responsabilidades:**
- Procesamiento de lenguaje natural
- Generación de respuestas conversacionales
- Clasificación de intenciones
- Extracción de entidades (futuro)

**Modelos:**
- `tinyllama` (1.1B) - Por defecto, más rápido
- `phi-2` (2.7B) - Mejor calidad, más recursos

**Despliegue:**
- Embebido en la misma VM que el middleware
- Puerto 11434 (default)
- Sin GPU, solo CPU

## Flujo de Datos

### Ejemplo: Usuario consulta una orden

```
1. Usuario (Widget): "¿Cuál es el estado de mi pedido SO001?"
   │
   ▼
2. Widget → POST /api/chat
   {
     "message": "¿Cuál es el estado de mi pedido SO001?",
     "conversationId": "conv-123"
   }
   │
   ▼
3. Middleware → Ollama: Clasificar intención
   Request: {
     "model": "tinyllama",
     "messages": [
       {"role": "system", "content": "Eres un asistente..."},
       {"role": "user", "content": "¿Cuál es el estado..."}
     ]
   }
   │
   ▼
4. Ollama → Middleware: Intención detectada
   Response: {
     "intent": "order_status",
     "entities": {"order_id": "SO001"}
   }
   │
   ▼
5. Middleware → Odoo MCP: Obtener orden
   MCP Request: {
     "method": "orders.get",
     "params": {"orderId": "SO001"}
   }
   │
   ▼
6. Odoo → Middleware: Datos de la orden
   MCP Response: {
     "orderId": "SO001",
     "status": "confirmed",
     "customerName": "Juan Pérez",
     "totalAmount": 1500.00
   }
   │
   ▼
7. Middleware → Ollama: Generar respuesta natural
   Request: {
     "model": "tinyllama",
     "messages": [
       {"role": "system", "content": "Formato de respuesta..."},
       {"role": "user", "content": "Orden SO001: confirmed, $1500"}
     ]
   }
   │
   ▼
8. Ollama → Middleware: Respuesta generada
   Response: {
     "response": "Tu pedido SO001 está confirmado por $1500..."
   }
   │
   ▼
9. Middleware → Widget: Respuesta final
   {
     "message": "Tu pedido SO001 está confirmado...",
     "data": {
       "orderId": "SO001",
       "status": "confirmed"
     }
   }
   │
   ▼
10. Widget → Usuario: Muestra respuesta formateada
```

## Decisiones de Arquitectura

### ADR-001: Ollama Embebido vs Servicio Externo

**Contexto:** Necesitamos capacidades LLM para el chatbot.

**Decisión:** Usar Ollama embebido en la misma VM que el middleware.

**Razones:**
- Reducir latencia (sin llamadas de red)
- Simplificar despliegue (un solo nodo)
- Reducir costos (sin API externa)
- Control total sobre el modelo

**Consecuencias:**
- Mayor uso de CPU/RAM en el nodo
- Limitado a modelos pequeños (< 3B parámetros)
- Escalado vertical (no horizontal)

### ADR-002: MCP como Protocolo de Integración

**Contexto:** Necesitamos integrar con Odoo para datos de negocio.

**Decisión:** Usar Model Context Protocol (MCP) en lugar de API REST directa.

**Razones:**
- Estándar emergente de Anthropic
- Semántica clara (operaciones sobre modelos)
- Versionado de contratos
- Preparado para agentes futuros

**Consecuencias:**
- Requiere implementar servidor MCP en Odoo
- Mayor complejidad inicial
- Mejor mantenibilidad a largo plazo

### ADR-003: Maven vs Gradle

**Contexto:** Necesitamos un build tool para el middleware Java.

**Decisión:** Usar Maven.

**Razones:**
- Más amplia adopción en empresas
- Configuración declarativa (XML)
- Ecosistema maduro de plugins
- Familiaridad del equipo

**Consecuencias:**
- Archivos de configuración más verbosos
- Menor flexibilidad que Gradle
- Pero más predecible y estándar

### ADR-004: Modelo Liviano (TinyLlama/Phi-2)

**Contexto:** Necesitamos ejecutar LLM en VM sin GPU.

**Decisión:** Usar modelos pequeños (1-3B parámetros).

**Razones:**
- Ejecución en CPU factible
- Latencia aceptable (< 2s)
- Menor consumo de RAM (< 4GB)
- Calidad suficiente para tareas específicas

**Consecuencias:**
- Capacidades limitadas comparado con modelos grandes
- Requiere prompts más específicos
- Posible fallback a respuestas template

## Observabilidad

### Logs Estructurados

Todos los componentes usan logs estructurados (JSON):

```json
{
  "timestamp": "2025-11-29T10:30:00Z",
  "level": "INFO",
  "service": "middleware",
  "correlationId": "abc123",
  "operation": "orders.get",
  "durationMs": 120,
  "status": "success",
  "metadata": {
    "orderId": "SO001"
  }
}
```

### Métricas

- **Widget:** Tiempo de respuesta, tasa de error
- **Middleware:** Latencia MCP, latencia Ollama, tasa de éxito
- **Ollama:** Tokens/segundo, uso de CPU/RAM

### Health Checks

```
GET /health
{
  "status": "UP",
  "components": {
    "ollama": {"status": "UP", "model": "tinyllama"},
    "odoo": {"status": "UP", "latency": 45},
    "diskSpace": {"status": "UP"}
  }
}
```

## Despliegue

### Configuración de VM

```yaml
Specs:
  - CPU: 4 vCPUs
  - RAM: 8 GB
  - Disco: 50 GB SSD
  - OS: Ubuntu 22.04 LTS

Servicios:
  - Ollama (puerto 11434)
  - Middleware (puerto 9000)
  - Nginx (puerto 80/443) - proxy reverso
```

### Secuencia de Arranque

```bash
1. systemctl start ollama
2. ollama pull tinyllama
3. systemctl start lujanita-middleware
4. nginx -s reload
```

## Seguridad

### Autenticación (Futuro)

- JWT tokens para autenticar clientes
- API keys para integraciones externas

### Autorización (Futuro)

- Roles: cliente, operador, admin
- Permisos por operación MCP

### Secrets

- Variables de entorno para credenciales
- No hardcodear en código
- Usar Secret Manager en producción

## Escalabilidad

### Corto Plazo

- Escalado vertical (más CPU/RAM en VM)
- Cache local (Redis) para reducir llamadas a Odoo
- Connection pooling para HTTP clients

### Largo Plazo

- Separar Ollama en nodo dedicado
- Múltiples instancias de middleware (load balancer)
- Queue para procesar mensajes asíncronos (RabbitMQ)

## Referencias

- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Ollama Documentation](https://ollama.ai/docs)
- [Spring Boot Best Practices](https://spring.io/guides)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

