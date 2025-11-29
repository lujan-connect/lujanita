# Backend API Specialist Agent - Implementation Example

**Agent Type**: Domain Specialist  
**Focus Area**: FastAPI backend development  
**SDD Phases**: Planning, Implementation, Testing

---

## 🎯 Agent Configuration

```yaml
agent:
  name: backend-api-specialist
  version: 1.0.0
  type: domain-specialist
  
expertise:
  primary:
    - FastAPI endpoint development
    - Pydantic v2 schema design
    - REST API patterns
    - pytest test creation
  
  secondary:
    - Firestore integration
    - Error handling patterns
    - API documentation
    - Observability (logging, metrics)

knowledge_base:
  required:
    - .github/copilot-knowledge/backend-api-patterns.md
    - .github/copilot-knowledge/domain-entities.md
    - .github/copilot-knowledge/testing-guide.md
    - docs/API_STYLE_GUIDE.md
  
  reference:
    - apps/api/schemas/
    - apps/api/app/routers/
    - apps/api/tests/
    - /specs/NNN-feature/spec.md (context-specific)

phases:
  - name: Planning
    tasks:
      - Define API contracts
      - Plan endpoint structure
      - Design schema models
  
  - name: Implementation
    tasks:
      - Create Pydantic schemas
      - Implement routers
      - Add error handling
      - Include observability
  
  - name: Testing
    tasks:
      - Write unit tests (test-first)
      - Create integration tests
      - Validate contracts

constraints:
  must:
    - Follow test-first methodology (TDD)
    - Use Pydantic v2 BaseModel
    - Include Field aliases for camelCase compatibility
    - Add structured logging
    - Define error codes
    - Document with docstrings
    - Export schemas in __init__.py
  
  must_not:
    - Modify frontend code
    - Change database schema without data-model.md update
    - Skip tests
    - Remove existing functionality
    - Add dependencies without approval

triggers:
  keywords:
    - "create API endpoint"
    - "add FastAPI router"
    - "implement REST API"
    - "add Pydantic schema"
    - "create backend endpoint"
  
  contexts:
    - Feature requires API endpoint
    - Spec defines HTTP interface
    - Task involves backend service
```

---

## 📖 Usage Examples

### Example 1: Create Complete CRUD Endpoint

**User Request**:
```markdown
@backend-api-specialist Create CRUD endpoints for Bookings:
- GET /bookings - List all bookings
- GET /bookings/{id} - Get single booking
- POST /bookings - Create booking
- PUT /bookings/{id} - Update booking
- DELETE /bookings/{id} - Delete booking

Context:
- Spec: /specs/002-booking-system/spec.md
- Entity: Booking (userId, serviceId, checkIn, checkOut, status)
- Phase: Implementation
```

**Agent Workflow**:

#### Step 1: Read Context
```python
# Agent reads:
# 1. /specs/002-booking-system/spec.md - Requirements
# 2. .github/copilot-knowledge/backend-api-patterns.md - Patterns
# 3. .github/copilot-knowledge/domain-entities.md - Entity structure
# 4. .github/copilot-knowledge/testing-guide.md - Test patterns
```

#### Step 2: Create Schema (Test-First)

**Test File**: `apps/api/tests/test_bookings.py`
```python
import pytest
from fastapi.testclient import TestClient
from datetime import datetime, timedelta
from app.main import app

client = TestClient(app)

def test_get_bookings_returns_empty_list():
    """Test GET /bookings returns empty list when no bookings exist."""
    response = client.get("/bookings")
    assert response.status_code == 200
    assert response.json() == []

def test_create_booking_success():
    """Test POST /bookings creates a new booking."""
    tomorrow = datetime.now() + timedelta(days=1)
    next_week = datetime.now() + timedelta(days=7)
    
    payload = {
        "userId": "user123",
        "serviceId": "service456",
        "checkIn": tomorrow.isoformat(),
        "checkOut": next_week.isoformat()
    }
    
    response = client.post("/bookings", json=payload)
    assert response.status_code == 201
    data = response.json()
    assert "id" in data
    assert data["userId"] == "user123"
    assert data["status"] == "pending"

def test_create_booking_invalid_dates():
    """Test POST /bookings fails when checkOut before checkIn."""
    tomorrow = datetime.now() + timedelta(days=1)
    yesterday = datetime.now() - timedelta(days=1)
    
    payload = {
        "userId": "user123",
        "serviceId": "service456",
        "checkIn": tomorrow.isoformat(),
        "checkOut": yesterday.isoformat()
    }
    
    response = client.post("/bookings", json=payload)
    assert response.status_code == 422
    assert response.json()["detail"]["code"] == "INVALID_DATES"

def test_get_booking_by_id_success():
    """Test GET /bookings/{id} returns booking details."""
    # First create a booking
    tomorrow = datetime.now() + timedelta(days=1)
    next_week = datetime.now() + timedelta(days=7)
    
    create_response = client.post("/bookings", json={
        "userId": "user123",
        "serviceId": "service456",
        "checkIn": tomorrow.isoformat(),
        "checkOut": next_week.isoformat()
    })
    booking_id = create_response.json()["id"]
    
    # Then retrieve it
    response = client.get(f"/bookings/{booking_id}")
    assert response.status_code == 200
    assert response.json()["id"] == booking_id

def test_get_booking_not_found():
    """Test GET /bookings/{id} returns 404 for non-existent booking."""
    response = client.get("/bookings/nonexistent")
    assert response.status_code == 404
    assert response.json()["detail"]["code"] == "BOOKING_NOT_FOUND"

def test_update_booking_success():
    """Test PUT /bookings/{id} updates booking."""
    # Create booking first
    tomorrow = datetime.now() + timedelta(days=1)
    next_week = datetime.now() + timedelta(days=7)
    
    create_response = client.post("/bookings", json={
        "userId": "user123",
        "serviceId": "service456",
        "checkIn": tomorrow.isoformat(),
        "checkOut": next_week.isoformat()
    })
    booking_id = create_response.json()["id"]
    
    # Update it
    update_payload = {
        "status": "confirmed"
    }
    response = client.put(f"/bookings/{booking_id}", json=update_payload)
    assert response.status_code == 200
    assert response.json()["status"] == "confirmed"

def test_delete_booking_success():
    """Test DELETE /bookings/{id} deletes booking."""
    # Create booking first
    tomorrow = datetime.now() + timedelta(days=1)
    next_week = datetime.now() + timedelta(days=7)
    
    create_response = client.post("/bookings", json={
        "userId": "user123",
        "serviceId": "service456",
        "checkIn": tomorrow.isoformat(),
        "checkOut": next_week.isoformat()
    })
    booking_id = create_response.json()["id"]
    
    # Delete it
    response = client.delete(f"/bookings/{booking_id}")
    assert response.status_code == 204
    
    # Verify it's gone
    get_response = client.get(f"/bookings/{booking_id}")
    assert get_response.status_code == 404
```

**Action**: Run tests (MUST FAIL)
```bash
cd apps/api && PYTHONPATH=. pytest tests/test_bookings.py -v
```
Expected: All tests FAIL (endpoints don't exist yet)

#### Step 3: Create Pydantic Schema

**File**: `apps/api/schemas/booking.py`
```python
from pydantic import BaseModel, Field, ConfigDict, field_validator
from datetime import datetime
from typing import Optional, Literal

class BookingBase(BaseModel):
    """Base booking schema with common fields.
    
    Attributes:
        user_id: ID of the user making the booking.
        service_id: ID of the service being booked.
        check_in: Check-in date and time.
        check_out: Check-out date and time.
    """
    user_id: str = Field(alias="userId")
    service_id: str = Field(alias="serviceId")
    check_in: datetime = Field(alias="checkIn")
    check_out: datetime = Field(alias="checkOut")
    
    @field_validator("check_out")
    @classmethod
    def check_out_after_check_in(cls, v: datetime, info) -> datetime:
        """Validate that check_out is after check_in."""
        if "check_in" in info.data and v <= info.data["check_in"]:
            raise ValueError("check_out must be after check_in")
        return v

class BookingCreate(BookingBase):
    """Schema for creating a new booking."""
    pass

class BookingUpdate(BaseModel):
    """Schema for updating an existing booking.
    
    Attributes:
        status: New booking status.
        check_in: Updated check-in date (optional).
        check_out: Updated check-out date (optional).
    """
    status: Optional[Literal["pending", "confirmed", "cancelled"]] = None
    check_in: Optional[datetime] = Field(None, alias="checkIn")
    check_out: Optional[datetime] = Field(None, alias="checkOut")

class Booking(BookingBase):
    """Complete booking schema with all fields.
    
    Attributes:
        id: Unique booking identifier.
        status: Current booking status.
        total_price: Total booking price (optional).
        created_at: Booking creation timestamp.
        updated_at: Last update timestamp.
    """
    model_config = ConfigDict(from_attributes=True)
    
    id: str
    status: Literal["pending", "confirmed", "cancelled"] = "pending"
    total_price: Optional[float] = Field(None, alias="totalPrice")
    created_at: datetime = Field(alias="createdAt")
    updated_at: datetime = Field(alias="updatedAt")
```

**Export Schema**: Update `apps/api/schemas/__init__.py`
```python
from .booking import Booking, BookingCreate, BookingUpdate

__all__ = [
    # ... existing exports ...
    "Booking",
    "BookingCreate",
    "BookingUpdate",
]
```

#### Step 4: Create Router

**File**: `apps/api/app/routers/bookings.py`
```python
from fastapi import APIRouter, HTTPException, status
from typing import List
from datetime import datetime
import logging
import uuid

from schemas import Booking, BookingCreate, BookingUpdate

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/bookings", tags=["bookings"])

# In-memory storage for demo (replace with Firestore in production)
bookings_db: dict[str, Booking] = {}

@router.get("", response_model=List[Booking])
async def list_bookings():
    """List all bookings.
    
    Returns:
        List of all bookings.
    """
    logger.info("list_bookings", extra={
        "operation": "list_bookings",
        "count": len(bookings_db)
    })
    return list(bookings_db.values())

@router.post("", response_model=Booking, status_code=status.HTTP_201_CREATED)
async def create_booking(booking_data: BookingCreate):
    """Create a new booking.
    
    Args:
        booking_data: Booking creation data.
    
    Returns:
        Created booking with assigned ID.
    
    Raises:
        HTTPException: If validation fails.
    """
    booking_id = str(uuid.uuid4())
    now = datetime.now()
    
    booking = Booking(
        id=booking_id,
        user_id=booking_data.user_id,
        service_id=booking_data.service_id,
        check_in=booking_data.check_in,
        check_out=booking_data.check_out,
        status="pending",
        created_at=now,
        updated_at=now
    )
    
    bookings_db[booking_id] = booking
    
    logger.info("create_booking", extra={
        "operation": "create_booking",
        "booking_id": booking_id,
        "user_id": booking_data.user_id,
        "service_id": booking_data.service_id
    })
    
    return booking

@router.get("/{booking_id}", response_model=Booking)
async def get_booking(booking_id: str):
    """Get booking by ID.
    
    Args:
        booking_id: Booking identifier.
    
    Returns:
        Booking details.
    
    Raises:
        HTTPException: If booking not found.
    """
    if booking_id not in bookings_db:
        logger.warning("get_booking_not_found", extra={
            "operation": "get_booking",
            "booking_id": booking_id,
            "status": "not_found"
        })
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "code": "BOOKING_NOT_FOUND",
                "message": f"Booking {booking_id} not found"
            }
        )
    
    logger.info("get_booking", extra={
        "operation": "get_booking",
        "booking_id": booking_id,
        "status": "success"
    })
    
    return bookings_db[booking_id]

@router.put("/{booking_id}", response_model=Booking)
async def update_booking(booking_id: str, update_data: BookingUpdate):
    """Update booking.
    
    Args:
        booking_id: Booking identifier.
        update_data: Fields to update.
    
    Returns:
        Updated booking.
    
    Raises:
        HTTPException: If booking not found.
    """
    if booking_id not in bookings_db:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "code": "BOOKING_NOT_FOUND",
                "message": f"Booking {booking_id} not found"
            }
        )
    
    booking = bookings_db[booking_id]
    
    # Update fields
    if update_data.status is not None:
        booking.status = update_data.status
    if update_data.check_in is not None:
        booking.check_in = update_data.check_in
    if update_data.check_out is not None:
        booking.check_out = update_data.check_out
    
    booking.updated_at = datetime.now()
    
    logger.info("update_booking", extra={
        "operation": "update_booking",
        "booking_id": booking_id,
        "updated_fields": update_data.model_dump(exclude_none=True)
    })
    
    return booking

@router.delete("/{booking_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_booking(booking_id: str):
    """Delete booking.
    
    Args:
        booking_id: Booking identifier.
    
    Raises:
        HTTPException: If booking not found.
    """
    if booking_id not in bookings_db:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail={
                "code": "BOOKING_NOT_FOUND",
                "message": f"Booking {booking_id} not found"
            }
        )
    
    del bookings_db[booking_id]
    
    logger.info("delete_booking", extra={
        "operation": "delete_booking",
        "booking_id": booking_id,
        "status": "deleted"
    })
```

#### Step 5: Register Router

**File**: `apps/api/app/main.py`
```python
# ... existing imports ...
from app.routers import bookings

# ... existing code ...

# Register routers
app.include_router(bookings.router, prefix="/api/v1")

# ... rest of main.py ...
```

#### Step 6: Run Tests (MUST PASS)

```bash
cd apps/api && PYTHONPATH=. pytest tests/test_bookings.py -v
```
Expected: All tests PASS ✅

#### Step 7: Validate

```bash
# Type checking
npm run type-check

# Linting
cd apps/api && ruff check . && cd -

# Format
cd apps/api && ruff format . && cd -
```

---

## 🎓 Agent Behavior Patterns

### Pattern 1: Always Test-First

```python
# ❌ WRONG - Implement first
1. Create schema
2. Create router
3. Write tests (maybe)

# ✅ CORRECT - Test first
1. Write failing tests
2. Verify tests FAIL
3. Create schema
4. Create router
5. Verify tests PASS
```

### Pattern 2: Include Observability

```python
# ❌ WRONG - No logging
@router.post("/bookings")
async def create_booking(data: BookingCreate):
    booking = Booking(...)
    return booking

# ✅ CORRECT - Structured logging
@router.post("/bookings")
async def create_booking(data: BookingCreate):
    logger.info("create_booking", extra={
        "operation": "create_booking",
        "user_id": data.user_id
    })
    booking = Booking(...)
    return booking
```

### Pattern 3: Error Codes

```python
# ❌ WRONG - Plain string error
raise HTTPException(status_code=404, detail="Not found")

# ✅ CORRECT - Structured error with code
raise HTTPException(
    status_code=404,
    detail={
        "code": "BOOKING_NOT_FOUND",
        "message": "Booking not found"
    }
)
```

---

## 📋 Checklist for Each Implementation

Before marking task complete, verify:

- [ ] Tests written BEFORE implementation
- [ ] Tests failed before implementation
- [ ] Tests pass after implementation
- [ ] Pydantic schema uses `ConfigDict(from_attributes=True)`
- [ ] Fields have `Field(alias="camelCase")` for frontend compatibility
- [ ] All schemas have Google-style docstrings
- [ ] Schema exported in `schemas/__init__.py`
- [ ] Router uses structured logging
- [ ] Router has error codes for all exceptions
- [ ] Router registered in `main.py`
- [ ] Type checking passes (`npm run type-check`)
- [ ] Linting passes (`ruff check`)
- [ ] Formatting applied (`ruff format`)
- [ ] Spec requirements met (cross-reference with spec.md)

---

## 🔗 Related Resources

- **Pattern Reference**: [backend-api-patterns.md](../copilot-knowledge/backend-api-patterns.md)
- **Entity Patterns**: [domain-entities.md](../copilot-knowledge/domain-entities.md)
- **Testing Guide**: [testing-guide.md](../copilot-knowledge/testing-guide.md)
- **API Style**: [API_STYLE_GUIDE.md](../../docs/API_STYLE_GUIDE.md)

---

**Version**: 1.0.0  
**Last Updated**: 2025-11-02  
**Agent Type**: Implementation Example
