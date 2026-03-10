// Базовые игровые типы — aligned with backend GameStateView
export type StatKey = 'energy' | 'health' | 'stress' | 'mood' | 'money' | 'selfEsteem';

export interface Stats {
  energy: number;
  health: number;
  stress: number;
  mood: number;
  money: number;
  selfEsteem: number;
}

export interface JobView {
  title: string;
  satisfaction: number;
  burnoutRisk: number;
}

export interface Player {
  id: string;
  name: string;
  stats: Stats;
  job: JobView;
  location: string;
  tags: Record<string, boolean>;
  skills: Record<string, number>;
  inventory: string[];
}

/**
 * ActionOption — matches backend ActionOptionView exactly.
 * No more hardcoded action lists on the frontend.
 */
export interface ActionOption {
  code: string;
  label: string;
  description: string;
  estimatedTimeCost: number;
  isAvailable: boolean;
  unavailableReason?: string | null;
  /** Future: animation key from atlas config, provided by backend */
  animationKey?: string | null;
}

export interface RelationshipView {
  npcId: string;
  name: string;
  closeness: number;
  trust: number;
  stability: number;
  romance: number;
}

export interface PetView {
  petId: string;
  petCode: string;
  name: string;
  satiety: number;
  attention: number;
  health: number;
  mood: number;
}

export interface QuestView {
  id: string;
  label: string;
  description: string;
  progressPercent: number;
  isCompleted: boolean;
}

export interface ConflictView {
  id: string;
  conflictCode: string;
  label: string;
  stage: string;
  playerCSP: number;
  opponentCSP: number;
  tactics: TacticOptionView[];
}

export interface TacticOptionView {
  code: string;
  label: string;
  description: string;
}

/** A single dialogue line shown before event choice buttons. */
export interface DialogueLineView {
  speaker: string; // "narrator" | "tanya" | "alexander" | "sam" | npc id
  textRu: string;
}

/** A single choice button in a narrative event. */
export interface EventOptionView {
  code: string;    // sent back to backend on selection
  labelRu: string; // button label shown to player
}

/**
 * Narrative event modal — matches backend EventView exactly.
 *
 * Rendering contract:
 *   1. Show titleRu as modal header
 *   2. Show descriptionRu as subtitle/flavour text
 *   3. Render each dialogue line as speech bubble (speaker + text)
 *   4. Render options as choice buttons
 *
 * Every event has at least one option (minimum: a single "Ок" button).
 */
export interface EventView {
  id: string;
  titleRu: string;
  descriptionRu: string;
  dialogue: DialogueLineView[];
  options: EventOptionView[];
}

export interface EndingView {
  type: string;
  category: string;
  summary: string;
}

export interface ActionResultView {
  actionCode: string;
  timeCost: number;
  description: string;
  statChanges: Record<string, number>;
  relationshipChanges: Record<string, number>;
  petMoodChanges: Record<string, number>;
}

export type ActionCode = string;
export type TacticCode = string;
export type EventChoiceCode = string;

/** Time slot value comes from backend XML config — kept as plain string. */
export type TimeSlot = string;

export interface GameTime {
  day: number;
  hour: number;
  timeSlot: string;
  dayOver?: boolean;
}

/**
 * NPC Activity View — current physical state of an NPC
 * Used to render NPC sprite/animation at correct location
 */
export interface NpcActivityView {
  npcId: string;
  displayName: string;
  category: string;        // "human", "cat", "dog"
  activityId: string;      // "breakfast", "sleeping", "phone_scroll"
  animationKey: string;    // "eating", "sleeping", "typing"
  locationId: string;      // "kitchen", "living_room", "away"
  moodSummary: string;     // dominant mood axis: "happy", "lonely", "irritated"
  isAvailable: boolean;    // can player interact right now?
}

/**
 * Domain Event View — narrative/NPC events from backend
 * Used to show toasts, logs, or trigger animations
 */
export interface DomainEventView {
  eventType: string;       // "NPC_ACTIVITY_CHANGED", "NPC_MOOD_EXTREME", etc.
  timestamp: string;       // ISO 8601
  payload: Record<string, unknown>;  // eventType-specific data
}

/**
 * GameState — matches backend GameStateView exactly.
 * This is the single source of truth for what the API returns.
 */
export interface GameState {
  sessionId: string;
  telegramUserId: string;
  player: Player;
  relationships: RelationshipView[];
  pets: PetView[];
  time: GameTime;
  availableActions: ActionOption[];
  activeQuests: QuestView[];
  completedQuestIds: string[];
  activeConflicts: ConflictView[];
  currentEvent: EventView | null;
  ending: EndingView | null;
  lastActionResult: ActionResultView | null;
  npcActivities: NpcActivityView[];
  domainEvents: DomainEventView[];
}
