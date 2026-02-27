// Базовые типы для игровой логики

export interface Stats {
  energy: number;      // 0-100
  health: number;      // 0-100
  stress: number;      // 0-100
  mood: number;        // 0-100
  money: number;       // рубли
  selfEsteem: number;  // 0-100
}

export interface Skills {
  empathy: number;         // 0-100
  charisma: number;        // 0-100
  assertiveness: number;   // 0-100
  endurance: number;       // 0-100
  strength: number;        // 0-100
  rhetoric: number;        // 0-100
  humor: number;           // 0-100
  intuition: number;       // 0-100
}

export interface Job {
  title: string;
  satisfaction: number;  // 0-100
}

export interface Player {
  id: string;
  name: string;
  stats: Stats;
  job: Job;
  location: string;
  tags: string[];
  skills: Skills;
  inventory: string[];
}

export interface Relationship {
  id: string;
  npcName: string;
  npcType: 'HUSBAND' | 'FATHER';
  closeness: number;    // 0-100
  trust: number;        // 0-100
  stability: number;    // 0-100
  romance?: number;     // 0-100 (только для мужа)
}

export interface Pet {
  id: string;
  name: string;
  type: 'CAT' | 'DOG';
  hunger: number;       // 0-100
  happiness: number;    // 0-100
  health: number;       // 0-100
}

export type TimeSlot = 'MORNING' | 'DAY' | 'EVENING' | 'NIGHT';

export interface GameTime {
  day: number;
  timeSlot: TimeSlot;
}

export type ActionCode = 
  | 'WORK'
  | 'DATE_WITH_HUSBAND'
  | 'FEED_PETS'
  | 'PLAY_WITH_PETS'
  | 'WALK_PETS'
  | 'VISIT_FATHER'
  | 'REST'
  | 'SOCIAL_MEDIA';

export interface ActionOption {
  code: ActionCode;
  name: string;
  description: string;
  timeCost: number;      // часы
  energyCost: number;
  available: boolean;
  unavailableReason?: string;
}

export interface Quest {
  id: string;
  title: string;
  description: string;
  progress: number;      // 0-100
  completed: boolean;
  reward: string;
}

export type TacticCode =
  | 'SURRENDER'
  | 'ASSERT'
  | 'COMPROMISE'
  | 'AVOID'
  | 'LISTEN_AND_UNDERSTAND'
  | 'USE_HUMOR'
  | 'LOGICAL_ARGUMENT'
  | 'EMOTIONAL_APPEAL'
  | 'SET_BOUNDARIES';

export interface Tactic {
  code: TacticCode;
  name: string;
  description: string;
  available: boolean;
  requiredSkill?: keyof Skills;
  requiredSkillLevel?: number;
}

export interface Conflict {
  id: string;
  type: 'WITH_HUSBAND' | 'WITH_FATHER' | 'INTERNAL';
  title: string;
  description: string;
  intensity: number;     // 1-10
  csp: number;          // Conflict Stress Points 0-100
  availableTactics: Tactic[];
}

export interface EventOption {
  code: string;
  text: string;
  preview: string;       // краткое описание последствий
}

export interface GameEvent {
  id: string;
  title: string;
  description: string;
  options: EventOption[];
}

export type EndingType = 
  | 'HARMONY'
  | 'CAREER_FOCUSED'
  | 'FAMILY_HAPPINESS'
  | 'BURNOUT'
  | 'LONELINESS';

export interface Ending {
  type: EndingType;
  title: string;
  description: string;
  achievedAt: string;  // ISO date
}

export interface ActionResult {
  success: boolean;
  message: string;
  changes: {
    stats?: Partial<Stats>;
    relationships?: Array<{
      npcId: string;
      changes: Partial<Relationship>;
    }>;
    pets?: Array<{
      petId: string;
      changes: Partial<Pet>;
    }>;
  };
}

export interface GameState {
  sessionId: string;
  telegramUserId: string;
  player: Player;
  relationships: Relationship[];
  pets: Pet[];
  time: GameTime;
  availableActions: ActionOption[];
  activeQuests: Quest[];
  completedQuestIds: string[];
  activeConflicts: Conflict[];
  currentEvent: GameEvent | null;
  ending: Ending | null;
  lastActionResult: ActionResult | null;
}
