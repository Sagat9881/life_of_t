/**
 * SceneModel - Type definitions based on RoomPage.xml and SCREENS_SPECIFICATION.xml
 * This model represents the structure of game locations without requiring image assets.
 */

export interface SceneModel {
  id: string;
  name: string;
  type: 'room' | 'park' | 'office';
  layers: SceneLayer[];
  objects: InteractiveObject[];
  npcs: NPC[];
  atmosphere: Atmosphere;
}

export interface SceneLayer {
  name: string;
  zIndex: number;
  type: 'Static' | 'Isometric' | 'Clickable' | 'UI';
  content?: string;
}

export interface InteractiveObject {
  id: string;
  type: string;
  category: 'rest' | 'work' | 'social' | 'self-care' | 'appearance';
  position: Position;
  sprite: {
    size: { width: number; height: number };
  };
  states: ObjectState[];
  actions: ObjectAction[];
  highlight?: {
    type: 'glow';
    color: string;
    opacity: number;
  };
}

export interface Position {
  x: number;
  y: number;
  zIndex: number;
}

export interface ObjectState {
  name: string;
  default?: boolean;
  active?: string;
}

export interface ObjectAction {
  id: string;
  label: string;
  icon: string;
  requirements?: {
    energy?: { min?: number };
    time?: { between?: string };
    state?: { current?: string };
  };
  effects?: {
    energy?: { cost?: number; restore?: number };
    time?: { advance?: string };
    happiness?: { change?: number };
    money?: { reward?: number };
    relationship?: { target?: string; change?: number };
  };
}

export interface NPC {
  id: string;
  type: string;
  position: Position;
  sprite: {
    size: { width: number; height: number };
  };
  behavior: {
    type: 'wander' | 'lazy';
    bounds?: string;
    speed?: number;
    favoriteSpots?: Array<{ near: string; weight?: number }>;
  };
  interaction?: {
    tap: boolean;
    action: {
      id: string;
      label: string;
      effects?: Record<string, any>;
    };
  };
}

export interface Atmosphere {
  timeOfDay: 'morning' | 'day' | 'evening' | 'night';
  lighting: {
    bright?: boolean;
    warm?: boolean;
    dimmed?: boolean;
    dark?: boolean;
  };
  colors: {
    background: string;
    windowLight?: string;
    ceilingLight?: string;
  };
}

/**
 * GameState - Runtime state of the game
 */
export interface GameState {
  location: 'room' | 'park' | 'office';
  character: {
    position: Position;
    state: 'idle' | 'walking' | 'working' | 'sleeping' | 'interaction' | 'emotion';
    variant?: string;
    emotion?: 'neutral' | 'happy' | 'sad' | 'tired' | 'love' | 'focused';
  };
  stats: {
    energy: number;
    happiness: number;
    health: number;
  };
  time: {
    current: string;
    date: string;
    weekday: string;
    dayNumber: number;
  };
  money: number;
  objectStates: Record<string, string>;
}