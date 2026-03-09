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

/** @deprecated — use ActionOption instead */
export interface GameAction {
  code: string;
  name: string;
  description: string;
  timeCost: number;
  energyCost?: number;
  effects?: Partial<Stats>;
  available: boolean;
  category?: string;
}

export interface RelationshipView {
  npcId: string;
  name: string;
  closeness: number;
  trust: number;
  stability: number;
  romance: number;
}

export interface NPC {
  id: string;
  name: string;
  relationship: number;
  avatarUrl?: string;
  type: 'husband' | 'father' | 'friend';
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

export interface Pet {
  id: string;
  name: string;
  type: 'cat' | 'dog';
  mood: number;
  hunger: number;
  avatarUrl?: string;
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

export interface EventOptionView {
  code: string;
  label: string;
  description: string;
}

export interface EventView {
  id: string;
  label: string;
  description: string;
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

export interface Conflict {
  id: string;
  description: string;
  csp: number;
  maxCSP: number;
  tactics: ConflictTactic[];
}

export interface ConflictTactic {
  code: string;
  name: string;
  description: string;
  successChance: number;
}

export interface GameEvent {
  id: string;
  title: string;
  description: string;
  choices: EventChoice[];
}

export interface EventChoice {
  code: string;
  text: string;
  consequences?: string;
}

export type ActionCode = string;
export type TacticCode = string;
export type EventChoiceCode = string;

export type TimeSlot = 'MORNING' | 'DAY' | 'EVENING' | 'NIGHT';

export interface GameTime {
  day: number;
  hour: number;
  timeSlot: TimeSlot;
}

export interface Relationship {
  npcId: string;
  level: number;
  trust: number;
  romance?: number;
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
}
