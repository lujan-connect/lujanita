# Domain Model Catalog - SDD Knowledge Base

**Artifact Type**: Catálogo de entidades para generación de código AI  
**SDD Phase**: Modelado de datos e implementación  
**Audience**: AI Agents (principal), equipo de desarrollo (referencia)

---

## Propósito

Este artefacto provee un **catálogo completo de entidades de dominio** para el chatbot Lujanita, con esquemas TypeScript (widget) y Java (middleware) lado a lado. Úsalo cuando generes código que involucre modelos de datos, relaciones entre entidades o definiciones de tipos.

---

## Referencia Rápida

| Entidad | TypeScript (Widget) | Java (Middleware) | Modelo Odoo | Propósito |
|---------|---------------------|-------------------|-------------|-----------|
| Order | `packages/contracts/src/order.ts` | `apps/middleware/.../Order.java` | `sale.order` | Órdenes de venta |
| Customer | `packages/contracts/src/customer.ts` | `apps/middleware/.../Customer.java` | `res.partner` | Datos de cliente |
| Product | `packages/contracts/src/product.ts` | `apps/middleware/.../Product.java` | `product.product` | Catálogo de productos |
| DeliveryOrder | `packages/contracts/src/delivery.ts` | `apps/middleware/.../Delivery.java` | `stock.picking` | Órdenes de entrega |
| ChatSession | `packages/contracts/src/chat.ts` | `apps/middleware/.../ChatSession.java` | N/A | Sesiones conversacionales |
| Intent | `packages/contracts/src/intent.ts` | `apps/middleware/.../Intent.java` | N/A | Clasificación de intención |

---

## Entity: User

### Purpose
Represents a user account with authentication, role, and company association.

### TypeScript Schema

**Location**: `packages/domain/src/user.ts`

```typescript
import { Role } from './role';

/**
 * Application user account.
 */
export interface User {
  /** Unique identifier */
  id: string;
  
  /** Associated company ID */
  companyId: string | null;
  
  /** User's first name */
  firstName: string;
  
  /** User's last name */
  lastName: string;
  
  /** Email address (unique) */
  email: string;
  
  /** User role */
  role: Role;
  
  /** Subscribed service IDs */
  services: string[];
  
  /** Invoice IDs */
  invoices: string[];
  
  /** Support ticket IDs */
  tickets: string[];
  
  /** Linked credit account ID */
  creditAccount: string | null;
  
  /** Account creation timestamp */
  createdAt: Date;
}
```

### Python Schema

**Location**: `apps/api/schemas/user.py`

```python
from pydantic import BaseModel, Field, ConfigDict
from datetime import datetime
from .enums import Role

class User(BaseModel):
    """Application user data.
    
    Attributes:
        id: Unique identifier.
        company_id: Owning company identifier.
        first_name: User first name.
        last_name: User last name.
        email: User email address.
        role: Role assigned to the user.
        services: IDs of subscribed services.
        invoices: IDs of invoices issued for the user.
        tickets: IDs of related support tickets.
        credit_account: Identifier of linked credit account.
        created_at: Record creation timestamp.
    """
    model_config = ConfigDict(from_attributes=True)
    
    id: str
    company_id: str | None = Field(None, alias="companyId")
    first_name: str = Field(alias="firstName")
    last_name: str = Field(alias="lastName")
    email: str
    role: Role = Role.CLIENT
    services: list[str] = []
    invoices: list[str] = []
    tickets: list[str] = []
    credit_account: str | None = Field(None, alias="creditAccount")
    created_at: datetime = Field(alias="createdAt")
```

### Relationships

```
User (1) ─────────── (1) Company
  │
  │ (1:N)
  ↓
ServiceSubscription
  │
  │ (1:N)
  ↓
Invoice, Ticket, CreditAccount
```

### Firestore Collection

- **Collection**: `users`
- **Document ID**: User ID (UUID)
- **Indexes**: `email` (unique), `companyId`, `role`

---

## Entity: Company

### Purpose
Company account that owns users and has microsite configuration.

### TypeScript Schema

**Location**: `packages/domain/src/company.ts`

```typescript
/**
 * Company account data.
 */
export interface Company {
  id: string;
  legalName: string;
  billingInfo: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state?: string;
  country: string;
  postalCode?: string;
  baseUrl?: string;
  username?: string;
  password?: string;
  micrositeId?: string;
  webhookToken?: string;
  users: string[];
  createdAt: Date;
}
```

### Python Schema

**Location**: `apps/api/schemas/company.py`

```python
class Company(BaseModel):
    """Company account data.
    
    Attributes:
        id: Unique identifier.
        legal_name: Registered legal name.
        billing_info: Billing information for invoicing.
        address_line1: Primary street address.
        address_line2: Secondary address line.
        city: City name.
        state: State or province.
        country: Country code or name.
        postal_code: Postal or ZIP code.
        base_url: Base URL of the company website.
        username: API username for token auth.
        password: API password for token auth.
        microsite_id: API micrositeId.
        webhook_token: Token to authenticate webhook calls.
        users: Associated user IDs.
        created_at: Record creation timestamp.
    """
    model_config = ConfigDict(from_attributes=True)
    
    id: str
    legal_name: str = Field(alias="legalName")
    billing_info: str = Field(alias="billingInfo")
    address_line1: str = Field(alias="addressLine1")
    address_line2: str | None = Field(None, alias="addressLine2")
    city: str
    state: str | None = None
    country: str
    postal_code: str | None = Field(None, alias="postalCode")
    base_url: str | None = Field(None, alias="baseUrl")
    username: str | None = None
    password: str | None = None
    microsite_id: str | None = Field(None, alias="micrositeId")
    webhook_token: str | None = Field(None, alias="webhookToken")
    users: list[str] = []
    created_at: datetime = Field(alias="createdAt")
```

### Relationships

```
Company (1) ─────────── (N) User
   │
   │ (1:1)
   ↓
Microsite
```

---

## Entity: Service

### Purpose
Service offering available for subscription by users.

### TypeScript Schema

**Location**: `packages/domain/src/service.ts`

```typescript
import { ServiceType, ServiceStatus, ServiceOptionKey } from './enums';

export interface Service {
  id: string;
  name: string;
  category: string;
  description: string;
  serviceType: ServiceType;
  serviceOptionKeys: ServiceOptionKey[];
  serviceOptions: Record<ServiceOptionKey, ServiceOption>;
  configSchema?: ConfigStep[];
  configPerExecution: boolean;
  executorName?: string;
  status: ServiceStatus;
  createdAt: Date;
}

export interface ServiceOption {
  initialPrice: number;
  monthlyPrice: number;
  perExecPrice: number;
  executionsIncluded: number;
  optionType: OptionType;
}
```

### Python Schema

**Location**: `apps/api/schemas/service.py`

```python
class ServiceOption(BaseModel):
    """Parameters for each Service option."""
    initial_price: float = Field(0, alias="initialPrice")
    monthly_price: float = Field(0, alias="monthlyPrice")
    per_exec_price: float = Field(0, alias="perExecPrice")
    executions_included: int = Field(0, alias="executionsIncluded")
    option_type: OptionType = Field(alias="optionType")

class Service(BaseModel):
    """Service offering available for subscription."""
    model_config = ConfigDict(from_attributes=True)
    
    id: str
    name: str
    category: str
    description: str
    service_type: ServiceType = Field(alias="serviceType")
    service_option_keys: list[ServiceOptionKey] = Field(alias="serviceOptionKeys")
    service_options: dict[ServiceOptionKey, ServiceOption] = Field(alias="serviceOptions")
    config_schema: list[ConfigStep] | None = Field(None, alias="configSchema")
    config_per_execution: bool = Field(False, alias="configPerExecution")
    executor_name: str | None = Field(None, alias="executorName")
    status: ServiceStatus = ServiceStatus.COMINGSOON
    created_at: datetime = Field(alias="createdAt")
```

---

## Entity: ServiceSubscription

### Purpose
Represents a user's subscription to a service with configuration and execution tracking.

### TypeScript Schema

**Location**: `packages/domain/src/service-subscription.ts`

```typescript
export interface ServiceSubscription {
  id: string;
  userId: string;
  serviceId: string;
  alias?: string;
  subscriptionStatus: SubscriptionStatus;
  serviceSubscriptionStatus: ServiceSubscriptionStatus;
  startDate: Date;
  endDate?: Date;
  dueDate?: Date;
  autoRenew: boolean;
  selectedOption: ServiceOptionKey;
  appliedMonthlyPrice: number;
  appliedPerExecPrice: number;
  appliedExecutionsIncluded: number;
  availableExecutions: number;
  serviceType?: ServiceType;
  optionType?: OptionType;
  config?: Record<string, any>;
}
```

### Python Schema

**Location**: `apps/api/schemas/service_subscription.py`

```python
class ServiceSubscription(BaseModel):
    """Association between a user and a subscribed service."""
    model_config = ConfigDict(from_attributes=True)
    
    id: str
    user_id: str = Field(alias="userId")
    service_id: str = Field(alias="serviceId")
    alias: str | None = None
    subscription_status: SubscriptionStatus = Field(SubscriptionStatus.ACTIVE, alias="subscriptionStatus")
    service_subscription_status: ServiceSubscriptionStatus = Field(ServiceSubscriptionStatus.DRAFT, alias="serviceSubscriptionStatus")
    start_date: datetime = Field(alias="startDate")
    end_date: datetime | None = Field(None, alias="endDate")
    due_date: datetime | None = Field(None, alias="dueDate")
    auto_renew: bool = Field(True, alias="autoRenew")
    selected_option: ServiceOptionKey = Field(alias="selectedOption")
    applied_monthly_price: float = Field(alias="appliedMonthlyPrice")
    applied_per_exec_price: float = Field(alias="appliedPerExecPrice")
    applied_executions_included: int = Field(alias="appliedExecutionsIncluded")
    available_executions: int = Field(alias="availableExecutions")
    service_type: ServiceType | None = Field(None, alias="serviceType")
    option_type: OptionType | None = Field(None, alias="optionType")
    config: dict | None = None
```

### Relationships

```
ServiceSubscription (N:1) → User
ServiceSubscription (N:1) → Service
ServiceSubscription (1:N) → ServiceExecution
```

---

## Enums & Types

### Role

```typescript
// TypeScript
export enum Role {
  CLIENT = "CLIENT",
  ADMIN = "ADMIN",
  SUPERADMIN = "SUPERADMIN"
}
```

```python
# Python
from enum import Enum

class Role(str, Enum):
    CLIENT = "CLIENT"
    ADMIN = "ADMIN"
    SUPERADMIN = "SUPERADMIN"
```

### ServiceType

```typescript
export enum ServiceType {
  BY_EXECUTION = "BY_EXECUTION",
  BY_VOLUME = "BY_VOLUME"
}
```

### SubscriptionStatus

```typescript
export enum SubscriptionStatus {
  ACTIVE = "ACTIVE",
  EXPIRED = "EXPIRED"
}
```

### ServiceStatus

```typescript
export enum ServiceStatus {
  COMINGSOON = "COMINGSOON",
  ENABLED = "ENABLED",
  DISABLED = "DISABLED"
}
```

---

## For AI: Adding a New Entity

### Step-by-Step Workflow

#### 1. Define in Spec First

Create `/specs/NNN-feature/data-model.md`:

```markdown
## Entity: MyEntity

### Purpose
[Why this entity exists]

### Attributes
- id (string, required): Unique identifier
- name (string, required): Entity name
- createdAt (datetime, required): Creation timestamp

### Relationships
- Belongs to User (N:1)
- Has many ChildEntity (1:N)
```

#### 2. Create TypeScript Interface

```typescript
// packages/domain/src/my-entity.ts

/**
 * Brief description.
 */
export interface MyEntity {
  id: string;
  name: string;
  createdAt: Date;
}
```

#### 3. Export TypeScript

```typescript
// packages/domain/src/index.ts
export * from "./my-entity";
```

#### 4. Create Python Schema

```python
# apps/api/schemas/my_entity.py
from pydantic import BaseModel, Field, ConfigDict
from datetime import datetime

class MyEntity(BaseModel):
    """Brief description."""
    model_config = ConfigDict(from_attributes=True)
    
    id: str
    name: str
    created_at: datetime = Field(alias="createdAt")
```

#### 5. Export Python Schema

```python
# apps/api/schemas/__init__.py
from .my_entity import MyEntity
__all__ = [..., "MyEntity"]
```

#### 6. Validate

```bash
npm run type-check
cd apps/api && PYTHONPATH=. pytest
```

---

## AI Decision Trees

### Should This Be a Separate Entity?

```
Does it have persistent data?
├─ YES
│  ├─ Multiple properties (>2)?
│  │  ├─ YES → Create entity
│  │  └─ NO → Consider embedded object
│  └─ Reused across multiple contexts?
│     ├─ YES → Create entity
│     └─ NO → Consider inline type
└─ NO → Use inline type or utility type
```

### TypeScript or Python First?

```
Where is the source of truth?
├─ Backend (API defines schema)
│  └─ Python first → Generate TypeScript from OpenAPI
└─ Frontend (UI-only model)
   └─ TypeScript first → Add Python when API needs it
```

---

## Naming Conventions

### File Names
- **TypeScript**: `kebab-case.ts` (e.g., `service-subscription.ts`)
- **Python**: `snake_case.py` (e.g., `service_subscription.py`)

### Type/Class Names
- **Both**: `PascalCase` (e.g., `ServiceSubscription`)

### Property Names
- **TypeScript**: `camelCase` (e.g., `userId`, `createdAt`)
- **Python**: `snake_case` with alias (e.g., `user_id = Field(alias="userId")`)

### Collection Names (Firestore)
- **Pattern**: `snake_case_plural` (e.g., `service_subscriptions`)

---

## Common Patterns

### Optional vs Required Fields

```typescript
// TypeScript
interface Entity {
  required: string;
  optional?: string;  // May be undefined
  nullable: string | null;  // Explicitly nullable
}
```

```python
# Python
class Entity(BaseModel):
    required: str
    optional: str | None = None  # Optional with default
    nullable: str | None  # Required but can be None
```

### Timestamps

Always use `Date` (TS) and `datetime` (Python) with camelCase alias:

```typescript
createdAt: Date;
updatedAt?: Date;
```

```python
created_at: datetime = Field(alias="createdAt")
updated_at: datetime | None = Field(None, alias="updatedAt")
```

### Arrays/Lists

```typescript
items: string[];
objects: ChildEntity[];
```

```python
items: list[str] = []
objects: list[ChildEntity] = []
```

### Nested Objects

```typescript
config: Record<string, any>;
metadata: { [key: string]: string };
```

```python
config: dict[str, any] = {}
metadata: dict[str, str] = {}
```

---

## Related SDD Artifacts

- **API Patterns**: `.github/sdd-artifacts/api-patterns.md`
- **Testing Patterns**: (to be created)
- **Frontend Patterns**: (to be created)
- **General Instructions**: `.github/copilot-instructions.md`

---

**Version**: 1.0.0  
**Last Updated**: 2025-10-30  
**SDD Compliance**: GitHub Spec-Driven Development Toolkit

