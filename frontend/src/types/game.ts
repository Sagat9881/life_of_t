// Game state types matching backend views

export interface GameState {
  sessionId: string;
  player: Player;
  relationships: Relationship[];
  pets: Pet[];
  time: GameTime;
  availableActions: ActionOption[];
  activeConflict?: Conflict;
  activeEvent?: GameEvent;
  ending?: Ending;
  lastActionResult?: ActionResult;
}

export interface Player {
  name: string;
  stats: Stats;
  job: JobInfo;
  location: string;
}

export interface Stats {
  energy: number;
  health: number;
  stress: number;
  mood: number;
  money: number;
  selfEsteem: number;
}

export interface GameTime {
  day: number;
  hour: number;
}

export interface Relationship {
  npcCode: string;
  name: string;
  closeness: number;
  trust: number;
  stability: number;
  romance: number;
  broken: boolean;
}

export interface Pet {
  id: string;
  name: string;
  type: string;
  satiety: number;
  attention: number;
  health: number;
  mood: number;
}

export interface ActionOption {
  type: string;
  displayName: string;
  description: string;
  timeCost: number;
  available: boolean;
  unavailableReason?: string;
  category?: string;
  statEffects?: Partial<Stats>;
}

export interface ActionResult {
  actionType: string;
  message: string;
  statChanges: Partial<Stats>;
  timeAdvanced?: number;
}

export interface Conflict {
  id: string;
  type: string;
  category: string;
  description: string;
  stage: string;
  round: number;
  maxRounds: number;
  playerCsp: number;
  opponentCsp: number;
  tactics: TacticOption[];
}

export interface TacticOption {
  id: string;
  name: string;
  description: string;
  successChance?: number;
}

export interface GameEvent {
  id: string;
  title: string;
  description: string;
  options: EventOption[];
}

export interface EventOption {
  id: string;
  text: string;
  consequences?: string;
}

export interface JobInfo {
  satisfaction: number;
  burnoutRisk: number;
}

export interface Ending {
  type: string;
  title: string;
  description: string;
}

// API request/response types
export interface StartGameRequest {
  playerName?: string;
}

export interface ExecuteActionRequest {
  sessionId: string;
  actionType: string;
}

export interface ConflictTacticRequest {
  sessionId: string;
  tacticId: string;
}

export interface EventOptionRequest {
  sessionId: string;
  eventId: string;
  optionId: string;
}
