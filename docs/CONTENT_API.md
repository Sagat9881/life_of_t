# Game Content API

## Overview

Content API provides game data definitions for **data-driven frontend**. Instead of hardcoding actions, quests, and conflicts in UI code, frontend loads this metadata once on startup and renders everything dynamically.

**Key benefits:**
- Add/modify game content by changing XML → no frontend code changes
- Consistent UI rendering across all game systems
- Easy A/B testing and balancing
- Better localization support

---

## Endpoints

All content endpoints:
- **Method:** GET
- **Authentication:** None (public game data)
- **Base path:** `/api/v1/content`
- **Cache:** HTTP ETag + 1 hour cache control
- **Version:** Included in every response

### 1. Get Actions

**GET** `/api/v1/content/actions`

Returns all available player actions with metadata.

**Response:**
```json
{
  "version": {
    "version": "1.0.0",
    "updatedAt": "2026-03-09T15:00:00Z"
  },
  "actions": [
    {
      "code": "WORK",
      "title": "Работа",
      "description": "Поработать в офисе",
      "tags": ["career", "income"],
      "energyCost": 30,
      "minEnergy": 30,
      "requiredSkills": {},
      "requiredTags": [],
      "forbiddenTags": [],
      "statEffects": {
        "energy": -30,
        "stress": 10,
        "mood": -5
      },
      "skillGains": {
        "professional": 2
      },
      "moneyGain": 1000,
      "durationMinutes": 240,
      "animationTrigger": "work",
      "iconName": "briefcase",
      "availableTimeOfDay": ["morning", "day"],
      "availableLocations": ["workplace"],
      "potentialConflictTypes": ["work_conflict"],
      "relatedQuestIds": ["career_quest_1"]
    }
  ]
}
```

**Usage:**
Frontend renders action buttons from `GameStateView.availableActions[]`, but uses `actions[code]` for titles, icons, descriptions.

---

### 2. Get Conflicts

**GET** `/api/v1/content/conflicts`

Returns conflict types with available tactics.

**Response:**
```json
{
  "version": {...},
  "conflicts": [
    {
      "type": "WORK_DEADLINE",
      "title": "Рабочий дедлайн",
      "description": "Начальник требует сделать работу в нереальные сроки",
      "tactics": [
        {
          "code": "SURRENDER",
          "title": "Уступить",
          "description": "Согласиться и работать сверхурочно",
          "skillRequirements": {},
          "stressReduction": -5,
          "relationshipEffects": {"boss": 5},
          "skillGains": {},
          "baseSuccessChance": 80,
          "skillSuccessModifiers": {}
        },
        {
          "code": "ASSERT",
          "title": "Настоять",
          "description": "Объяснить, что сроки нереальны",
          "skillRequirements": {"assertiveness": 30},
          "stressReduction": 15,
          "relationshipEffects": {"boss": -10},
          "skillGains": {"assertiveness": 2},
          "baseSuccessChance": 40,
          "skillSuccessModifiers": {"assertiveness": 20}
        }
      ],
      "baseStressPoints": 50
    }
  ]
}
```

**Usage:**
Conflict UI shows `GameStateView.activeConflicts[]`, but renders tactic buttons from `conflicts[type].tactics[]`. Grayed out if skill requirements not met.

---

### 3. Get Quests

**GET** `/api/v1/content/quests`

Returns quest definitions with steps and rewards.

**Response:**
```json
{
  "version": {...},
  "quests": [
    {
      "id": "CAREER_START",
      "title": "Начало карьеры",
      "description": "Освоиться на новой работе",
      "category": "career",
      "requiredTags": [],
      "minSkills": {},
      "minDay": 1,
      "steps": [
        {
          "id": "work_first_week",
          "description": "Отработать первую неделю",
          "type": "ACTION",
          "requiredActions": ["WORK"],
          "requiredEventChoice": null,
          "requiredConflictResolution": null,
          "requiredSkillChecks": {}
        }
      ],
      "moneyReward": 2000,
      "skillRewards": {"professional": 10},
      "tagsGranted": ["career_started"],
      "completionMessage": "Ты успешно влилась в коллектив!"
    }
  ]
}
```

**Usage:**
Quest panel shows `GameStateView.activeQuests[]`, renders progress/steps from `quests[id]`.

---

### 4. Get Events

**GET** `/api/v1/content/events`

Returns narrative events with choice options.

**Response:**
```json
{
  "version": {...},
  "events": [
    {
      "id": "NEIGHBOR_COMPLAIN",
      "title": "Жалоба соседей",
      "description": "Соседи снизу жалуются на шум от Дюка",
      "category": "home",
      "requiredTags": [],
      "minSkills": {},
      "minDay": 7,
      "maxDay": null,
      "options": [
        {
          "code": "APOLOGIZE",
          "text": "Извиниться и пообещать следить за щенком",
          "requiredSkills": {},
          "requiredTags": [],
          "statEffects": {"stress": 5, "selfEsteem": -5},
          "relationshipEffects": {"neighbors": 10},
          "skillGains": {"empathy": 1},
          "moneyEffect": 0,
          "tagsGranted": [],
          "tagsRemoved": [],
          "outcomeMessage": "Соседи приняли извинения, но будут следить."
        }
      ],
      "priority": 5,
      "repeatable": false
    }
  ]
}
```

**Usage:**
When `GameStateView.currentEvent` is present, show event modal. Options come from `events[currentEvent.eventId].options[]`.

---

## Versioning

### Version Format

```typescript
interface ContentVersion {
  version: string;       // Semantic version "1.0.0"
  updatedAt: string;     // ISO timestamp
}
```

### How Versioning Works

1. **Server generates version** on content load (app startup)
2. **Client caches content** with version key
3. **ETag header** enables HTTP 304 Not Modified
4. **Version changes** when narrative XML files are updated

### Frontend Caching Strategy

```typescript
// Check version on app start
const cachedVersion = localStorage.getItem('contentVersion');
const response = await fetch('/api/v1/content/actions');

if (response.headers.get('ETag') === cachedVersion) {
  // Use cached content
  return JSON.parse(localStorage.getItem('actionsContent'));
} else {
  // Download and cache new content
  const data = await response.json();
  localStorage.setItem('contentVersion', data.version.version);
  localStorage.setItem('actionsContent', JSON.stringify(data.actions));
  return data.actions;
}
```

---

## Frontend Integration

### 1. ContentStore Setup

Create a Zustand store:

```typescript
interface ContentState {
  actions: Record<string, ActionDefView>;
  conflicts: Record<string, ConflictDefView>;
  quests: Record<string, QuestDefView>;
  events: Record<string, EventDefView>;
  version: ContentVersion | null;
  isLoaded: boolean;
  error: string | null;
  
  loadAllContent: () => Promise<void>;
}

const useContentStore = create<ContentState>((set) => ({
  actions: {},
  conflicts: {},
  quests: {},
  events: {},
  version: null,
  isLoaded: false,
  error: null,
  
  loadAllContent: async () => {
    try {
      const [actions, conflicts, quests, events] = await Promise.all([
        fetch('/api/v1/content/actions').then(r => r.json()),
        fetch('/api/v1/content/conflicts').then(r => r.json()),
        fetch('/api/v1/content/quests').then(r => r.json()),
        fetch('/api/v1/content/events').then(r => r.json()),
      ]);
      
      set({
        actions: Object.fromEntries(actions.actions.map(a => [a.code, a])),
        conflicts: Object.fromEntries(conflicts.conflicts.map(c => [c.type, c])),
        quests: Object.fromEntries(quests.quests.map(q => [q.id, q])),
        events: Object.fromEntries(events.events.map(e => [e.id, e])),
        version: actions.version,
        isLoaded: true
      });
    } catch (error) {
      set({ error: error.message });
    }
  }
}));
```

### 2. App Initialization

```typescript
function App() {
  const { loadAllContent, isLoaded } = useContentStore();
  const [gameState, setGameState] = useState(null);
  
  useEffect(() => {
    // Load content + start session in parallel
    Promise.all([
      loadAllContent(),
      startGameSession(telegramUserId)
    ]).then(([_, state]) => {
      setGameState(state);
    });
  }, []);
  
  if (!isLoaded || !gameState) {
    return <LoadingScreen />;
  }
  
  return <GameUI />;
}
```

### 3. Data-Driven Components

**Action List Example:**

```typescript
function ActionList() {
  const gameState = useGameState();
  const { actions } = useContentStore();
  
  return (
    <div>
      {gameState.availableActions.map(option => {
        const def = actions[option.actionCode];
        return (
          <ActionButton
            key={option.actionCode}
            title={def.title}
            description={def.description}
            icon={def.iconName}
            energyCost={def.energyCost}
            disabled={gameState.player.stats.energy < def.minEnergy}
            onClick={() => executeAction(option.actionCode)}
          />
        );
      })}
    </div>
  );
}
```

**Conflict UI Example:**

```typescript
function ConflictResolver({ conflict }) {
  const { conflicts } = useContentStore();
  const { player } = useGameState();
  const conflictDef = conflicts[conflict.type];
  
  return (
    <Modal>
      <h2>{conflictDef.title}</h2>
      <p>{conflictDef.description}</p>
      <TacticList>
        {conflictDef.tactics.map(tactic => {
          const isLocked = Object.entries(tactic.skillRequirements)
            .some(([skill, min]) => player.skills[skill] < min);
          
          return (
            <TacticButton
              key={tactic.code}
              title={tactic.title}
              description={tactic.description}
              disabled={isLocked}
              requiredSkills={tactic.skillRequirements}
              onClick={() => resolveConflict(conflict.id, tactic.code)}
            />
          );
        })}
      </TacticList>
    </Modal>
  );
}
```

---

## Development Roadmap

### Phase 1: Placeholders (Current)
- ✅ DTOs defined
- ✅ Controllers created
- ✅ Placeholder data in GameContentService
- ⏳ Frontend integration pending

### Phase 2: XML Integration
- Parse narrative XML files
- Map XML structures to DTOs
- Remove placeholder data
- Add content validation

### Phase 3: Hot Reload
- Watch narrative XML for changes
- Invalidate content cache
- Push version updates via SSE
- Frontend auto-refresh

---

## Testing

### Manual Testing via Swagger UI

1. Start backend: `mvn spring-boot:run`
2. Open http://localhost:8080/swagger-ui/index.html
3. Navigate to "Game Content" section
4. Test each endpoint:
   - GET `/api/v1/content/actions`
   - GET `/api/v1/content/conflicts`
   - GET `/api/v1/content/quests`
   - GET `/api/v1/content/events`

### Frontend Integration Test

```typescript
// ContentStore.test.ts
test('loads all content on init', async () => {
  const store = useContentStore.getState();
  await store.loadAllContent();
  
  expect(store.isLoaded).toBe(true);
  expect(Object.keys(store.actions).length).toBeGreaterThan(0);
  expect(store.version).toBeDefined();
});

test('caches content version', async () => {
  await loadAllContent();
  const version1 = useContentStore.getState().version;
  
  // Second load should use cache
  await loadAllContent();
  const version2 = useContentStore.getState().version;
  
  expect(version1).toEqual(version2);
});
```

---

## See Also

- [Event System](./EVENT_SYSTEM.md) — SSE-based real-time updates
- [Game State API](./GAME_STATE_API.md) — Player state and actions
- [Frontend System Prompt](./prompts/FRONTEND_SYSTEM_PROMPT.md) — UI guidelines
