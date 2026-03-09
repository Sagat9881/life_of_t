/**
 * Location type definitions for Canvas-based scene rendering
 */

export interface FurniturePlacement {
  readonly id: string;
  readonly entityName: string;
  readonly animation: string;
  readonly x: number;          // 0–100 (% of scene width)
  readonly y: number;          // 0–100 (% of scene height)
  readonly sceneHeight: number; // 0–100 (% of scene height)
  readonly scale: number;       // multiplier, default 1
  readonly zOrder: number;
  readonly actionCode?: string;
  readonly label?: string;
}

export interface CharacterSlot {
  readonly id: string;
  readonly entityName: string;
  readonly defaultAnimation: string;
  readonly x: number;          // 0–100
  readonly y: number;          // 0–100
  readonly sceneHeight: number; // 0–100 (% of scene height)
  readonly scale: number;
  readonly zOrder: number;
}

export interface LocationConfig {
  readonly id: string;
  readonly name: string;
  readonly locationAsset: string;
  readonly backgroundAnimation: string;
  readonly furniture: readonly FurniturePlacement[];
  readonly characters: readonly CharacterSlot[];
}
