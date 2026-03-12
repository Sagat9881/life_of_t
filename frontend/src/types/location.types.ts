export interface FurniturePlacement {
  readonly id: string;
  readonly assetKey: string;
  readonly x: number;
  readonly y: number;
  readonly width: number;
  readonly height: number;
  readonly zOrder: number;
  readonly actionCode?: string;
  readonly label?: string;
  readonly animationKey?: string;
}

export interface CharacterSlot {
  readonly id: string;
  readonly x: number;
  readonly y: number;
  readonly width: number;
  readonly height: number;
  readonly zOrder: number;
  readonly defaultAnimation: string;
}

export interface LocationConfig {
  readonly id: string;
  readonly name: string;
  readonly locationAsset: string;
  readonly backgroundAnimation: string;
  readonly backgroundAnimations?: Readonly<Record<string, string>>;
  readonly furniture: readonly FurniturePlacement[];
  readonly characters: readonly CharacterSlot[];
}
