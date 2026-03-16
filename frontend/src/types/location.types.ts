export interface FurniturePlacement {
  readonly id: string;
  readonly assetKey: string;
  readonly entityName: string;
  readonly x: number;
  readonly y: number;
  readonly width: number;
  readonly height: number;
  readonly sceneHeight: number;
  readonly scale: number;
  readonly zOrder: number;
  readonly flipX?: boolean;
  readonly actionCode?: string;
  readonly label?: string;
  readonly animation: string;
}

export interface CharacterSlot {
  readonly id: string;
  readonly entityName: string;
  readonly x: number;
  readonly y: number;
  readonly width: number;
  readonly height: number;
  readonly sceneHeight: number;
  readonly scale: number;
  readonly zOrder: number;
  readonly defaultAnimation: string;
}

export interface LocationConfig {
  readonly id: string;
  readonly name: string;
  readonly locationAsset: string;
  readonly backgroundAnimation: string;
  readonly furniture: readonly FurniturePlacement[];
  readonly characters: readonly CharacterSlot[];
}
