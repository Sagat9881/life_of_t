# Event System Architecture

## Overview

**Data-driven, zero-boilerplate event broadcasting** from backend to frontend using Server-Sent Events (SSE).

### Key Benefits

✅ **100% Generic** — No DTO mapping, no event-specific code  
✅ **Zero Frontend Changes** — Add new events by just creating backend records  
✅ **Type-Safe** — Backend enforces `DomainEvent` interface  
✅ **Real-Time** — SSE pushes events instantly to connected clients  
✅ **Reconnection** — Automatic reconnect with exponential backoff  
✅ **Multi-Tab** — Multiple browser tabs can subscribe to same session  

---

## Architecture

```
Backend (Spring)                         Frontend (React)
├─ Domain Services                       ├─ useEventStream()
│  └─ applicationEventPublisher          │  └─ Connect to SSE
│     .publishEvent(new ActionExecuted)  │
├─ DomainEventListener                   ├─ useEventHandler()
│  └─ @EventListener                     │  └─ Subscribe to specific types
│     └─ Serialize to JSON (reflection)  │
├─ EventBroadcaster                      └─ EventStreamService
│  └─ SSE emitters per sessionId            └─ Dispatch to handlers
└─ EventStreamController
   └─ GET /api/events/stream/{sessionId}
```

---

## Backend Usage

### 1. Define Event (Java Record)

```java
package ru.lifegame.backend.domain.event.domain;

import java.time.Instant;

public record ActionExecutedEvent(
    String sessionId,
    String actionCode,
    int energyDelta,
    int moodDelta
) implements DomainEvent {
    @Override public Instant timestamp() { return Instant.now(); }
    @Override public String eventType() { return "ACTION_EXECUTED"; }
}
```

**That's it!** No DTO, no mapper, no controller changes needed.

### 2. Publish Event

```java
@Service
public class ActionExecutionService {
    private final ApplicationEventPublisher eventPublisher;

    public void executeAction(String sessionId, String actionCode) {
        // ... execute action logic ...
        
        // Publish event - will be auto-broadcast to frontend
        eventPublisher.publishEvent(
            new ActionExecutedEvent(sessionId, actionCode, -10, +5)
        );
    }
}
```

---

## Frontend Usage

### 1. Connect to Event Stream

```tsx
import { useEventStream } from '@/hooks/useEventStream';

function GameView() {
  const { isConnected, status } = useEventStream({ 
    sessionId: 'player-session-123' 
  });

  return (
    <div>
      Connection: {status}
      {/* Your game UI */}
    </div>
  );
}
```

### 2. Handle Specific Event Types

```tsx
import { useEventHandler } from '@/hooks/useEventHandler';

function StatsDisplay() {
  const [energy, setEnergy] = useState(100);

  // Subscribe to ACTION_EXECUTED events
  useEventHandler('ACTION_EXECUTED', (event) => {
    console.log('Action executed:', event.actionCode);
    
    // Access fields dynamically (they're in the event object)
    if (event.energyDelta) {
      setEnergy(prev => prev + Number(event.energyDelta));
    }
  });

  return <div>Energy: {energy}</div>;
}
```

### 3. Handle Multiple Event Types

```tsx
import { useEventHandlers } from '@/hooks/useEventHandler';

function GameController() {
  useEventHandlers({
    ACTION_EXECUTED: (e) => {
      console.log('Action:', e.actionCode);
    },
    
    NPC_ACTIVITY_CHANGED: (e) => {
      console.log('NPC changed activity:', e.npcId, e.newActivity);
    },
    
    NARRATIVE_EVENT_TRIGGERED: (e) => {
      console.log('Story event:', e.title, e.options);
    },
  });

  return <GameUI />;
}
```

### 4. Global Event Handler (Logging/Analytics)

```tsx
import { useGlobalEventHandler } from '@/hooks/useEventHandler';

function AnalyticsProvider({ children }) {
  useGlobalEventHandler((event) => {
    // Receives ALL events
    analytics.track(event.eventType, {
      timestamp: event.timestamp,
      ...event,
    });
  });

  return <>{children}</>;
}
```

---

## Adding New Events

### Backend Only (3 steps)

1. **Create record** in `domain/event/domain/`
2. **Publish it** via `ApplicationEventPublisher`
3. **Done** — Frontend receives it automatically

### Frontend (optional)

- If you want to **react** to the new event, add a handler with `useEventHandler()`
- If you just want to **log** it, no changes needed (global handler catches all)

---

## Event Flow Example

```
User clicks "Work" button
  ↓
Frontend calls POST /api/actions/execute { actionCode: "work_office" }
  ↓
Backend executes action
  ↓
Backend publishes ActionExecutedEvent(sessionId, "work_office", -15, +10)
  ↓
DomainEventListener intercepts it
  ↓
Serialized to JSON: { eventType: "ACTION_EXECUTED", actionCode: "work_office", ... }
  ↓
EventBroadcaster pushes via SSE to all subscribers of sessionId
  ↓
Frontend EventStreamService receives it
  ↓
Dispatches to registered handlers
  ↓
useEventHandler('ACTION_EXECUTED', handler) callback fires
  ↓
UI updates (energy bar, animation, etc.)
```

---

## Connection Management

### Auto-Reconnect

`EventStreamService` automatically reconnects on connection loss:
- Exponential backoff: 3s, 6s, 9s, 12s, 15s
- Max 5 attempts
- Status exposed via `useEventStream().status`

### Multi-Tab Support

Each browser tab creates a separate SSE connection. All tabs subscribed to the same `sessionId` receive the same events.

### Health Check

```bash
GET /api/events/stream/{sessionId}/health
# Returns: { "sessionId": "...", "subscriberCount": 2, "connected": true }
```

---

## Event Structure

### Backend (DomainEvent)

```java
public interface DomainEvent {
    String sessionId();   // Required
    Instant timestamp();  // Required
    String eventType();   // Required (e.g., "ACTION_EXECUTED")
}
```

### Frontend (DomainEvent)

```typescript
interface DomainEvent {
  eventType: string;    // Same as backend
  timestamp: number;    // Unix millis
  sessionId: string;    // Same as backend
  [key: string]: unknown; // All other fields dynamic
}
```

---

## Testing

### Backend

```java
@Test
void shouldBroadcastEvent() {
    // Given
    String sessionId = "test-session";
    
    // When
    eventPublisher.publishEvent(
        new ActionExecutedEvent(sessionId, "work", -10, +5)
    );
    
    // Then - verify EventBroadcaster.broadcast() was called
    verify(eventBroadcaster).broadcast(
        eq(sessionId), 
        eq("ACTION_EXECUTED"), 
        argThat(payload -> 
            payload.get("actionCode").equals("work") &&
            payload.get("energyDelta").equals(-10)
        )
    );
}
```

### Frontend

```tsx
import { renderHook } from '@testing-library/react';
import { useEventHandler } from '@/hooks/useEventHandler';

test('event handler receives events', () => {
  const handler = jest.fn();
  
  renderHook(() => useEventHandler('ACTION_EXECUTED', handler));
  
  // Simulate SSE event
  eventStreamService.handleIncomingEvent({
    data: JSON.stringify({
      eventType: 'ACTION_EXECUTED',
      actionCode: 'work',
      energyDelta: -10,
    }),
  });
  
  expect(handler).toHaveBeenCalledWith(
    expect.objectContaining({ actionCode: 'work' })
  );
});
```

---

## Performance Considerations

- **SSE Timeout**: 30 minutes (configurable in `EventBroadcaster`)
- **Reconnect Delay**: 3s base with exponential backoff
- **Event Size**: Keep payloads small (<1KB recommended)
- **Handler Performance**: Avoid blocking operations in event handlers

---

## Existing Events (as of 2026-03-09)

### Character Actions
- `ACTION_EXECUTED` — Player performed an action

### NPC Behavior  
- `NPC_ACTIVITY_CHANGED` — NPC switched activity
- `NPC_MOOD_EXTREME` — NPC mood reached extreme (very happy/angry)

### Story & Quests
- `NARRATIVE_EVENT_TRIGGERED` — Story event with choices
- `EVENT_TRIGGERED` — Generic game event
- `QUEST_PROGRESS_UPDATED` — Quest progress changed
- `QUEST_STEP_COMPLETED` — Quest step finished

### Conflicts & Relationships
- `CONFLICT_TRIGGERED` — New conflict started
- `CONFLICT_TACTIC_APPLIED` — Player chose conflict tactic
- `CONFLICT_RESOLVED` — Conflict ended
- `RELATIONSHIP_BROKEN` — Relationship threshold crossed

### Game Flow
- `DAY_ENDED` — Day transition
- `ENDING_ACHIEVED` — Game ending reached
- `GAME_OVER` — Game ended

**Add new events by creating records — no list update needed!**
