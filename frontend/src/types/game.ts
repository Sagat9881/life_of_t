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

export interface Player {
  id: string;
  name: string;
  level: number;
  stats: Stats;
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
  mood: number;
  hunger: number;
  avatarUrl?: string;
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
