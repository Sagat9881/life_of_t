// Базовые игровые типы
export type StatKey = 'energy' | 'health' | 'stress' | 'mood' | 'money' | 'selfEsteem';

export interface Stats {
  energy: number;
  health: number;
  stress: number;
  mood: number;
  money: number;
  selfEsteem: number;
}

export interface Job {
  title: string;
  company: string;
  salary: number;
}

export interface Player {
  id: string;
  name: string;
  level: number;
  stats: Stats;
  job?: Job;
  avatarUrl?: string;
}

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

export interface NPC {
  id: string;
  name: string;
  relationship: number;
  avatarUrl?: string;
  type: 'husband' | 'father' | 'friend';
}

export interface Pet {
  id: string;
  name: string;
  type: 'cat' | 'dog';
  species: 'Cat' | 'Dog'; // Backend uses this
  mood: number;
  hunger: number;
  avatarUrl?: string;
}

export interface Quest {
  id: string;
  title: string;
  description: string;
  completed: boolean;
  progress?: number;
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

// Дополнительные типы для API и store
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

export interface GameState {
  player: Player;
  time: GameTime;
  actions: GameAction[];
  npcs: NPC[];
  pets: Pet[];
  relationships: Relationship[];
  availableActions: GameAction[];
  activeConflicts: Conflict[];
  currentConflict?: Conflict;
  currentEvent?: GameEvent;
  quests?: Quest[];
}
