/**
 * Location configuration — compositing layer approach.
 *
 * ── COORDINATE SYSTEM ──
 * x/y: percentage (0–100) relative to scene viewport (640×480).
 * Entities are positioned at these % coords.
 *
 * ── FURNITURE SIZING ──
 * sceneHeight: fraction of SCENE_HEIGHT (0..1) this item occupies.
 *   e.g. 0.35 = 35% of 480px = 168px tall.
 * scale: optional multiplier on top of sceneHeight. Default 1.0.
 *
 * Furniture renders as static composite PNG (no atlas animation needed).
 * Characters render via SpriteAnimator with atlas.
 */

export interface FurniturePlacement {
  readonly id: string;
  readonly entityName: string;
  readonly animation: string;
  readonly x: number;
  readonly y: number;
  /** Fraction of scene height this furniture occupies (0..1) */
  readonly sceneHeight: number;
  readonly scale: number;
  readonly zOrder: number;
  readonly actionCode?: string;
  readonly label?: string;
}

export interface CharacterSlot {
  readonly id: string;
  readonly entityName: string;
  readonly defaultAnimation: string;
  readonly x: number;
  readonly y: number;
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

export const LOCATIONS: Record<string, LocationConfig> = {
  home_room: {
    id: 'home_room',
    name: 'Комната Татьяны',
    locationAsset: 'home_room',
    backgroundAnimation: 'idle',
    furniture: [
      {
        id: 'bookshelf',
        entityName: 'bookshelf',
        animation: 'idle',
        x: 12,
        y: 72,
        sceneHeight: 0.55,
        scale: 1,
        zOrder: 8,
      },
      {
        id: 'bed',
        entityName: 'bed',
        animation: 'idle',
        x: 22,
        y: 92,
        sceneHeight: 0.40,
        scale: 1,
        zOrder: 12,
        actionCode: 'REST_AT_HOME',
        label: 'Кровать',
      },
      {
        id: 'computer',
        entityName: 'computer',
        animation: 'idle',
        x: 52,
        y: 85,
        sceneHeight: 0.45,
        scale: 1,
        zOrder: 10,
        actionCode: 'WORK_ON_PROJECT',
        label: 'Компьютер',
      },
      {
        id: 'phone',
        entityName: 'phone',
        animation: 'idle',
        x: 68,
        y: 92,
        sceneHeight: 0.12,
        scale: 1,
        zOrder: 15,
        actionCode: 'CALL_HUSBAND',
        label: 'Телефон',
      },
      {
        id: 'mirror',
        entityName: 'mirror',
        animation: 'idle',
        x: 78,
        y: 72,
        sceneHeight: 0.40,
        scale: 1,
        zOrder: 10,
        actionCode: 'BEAUTY_ROUTINE',
        label: 'Зеркало',
      },
      {
        id: 'cat_tree',
        entityName: 'cat_tree',
        animation: 'idle',
        x: 92,
        y: 80,
        sceneHeight: 0.45,
        scale: 1,
        zOrder: 8,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 38,
        y: 92,
        scale: 1,
        zOrder: 50,
      },
      {
        id: 'alexander',
        entityName: 'alexander',
        defaultAnimation: 'idle',
        x: 56,
        y: 92,
        scale: 1,
        zOrder: 48,
      },
    ],
  },

  kitchen: {
    id: 'kitchen',
    name: 'Кухня',
    locationAsset: 'kitchen',
    backgroundAnimation: 'idle',
    furniture: [
      {
        id: 'stove',
        entityName: 'stove',
        animation: 'idle',
        x: 20,
        y: 85,
        sceneHeight: 0.40,
        scale: 1,
        zOrder: 10,
        actionCode: 'COOK_FOOD',
        label: 'Плита',
      },
      {
        id: 'fridge',
        entityName: 'fridge',
        animation: 'idle',
        x: 78,
        y: 78,
        sceneHeight: 0.50,
        scale: 1,
        zOrder: 10,
        actionCode: 'EAT_FOOD',
        label: 'Холодильник',
      },
      {
        id: 'pet_bowl',
        entityName: 'pet_bowl',
        animation: 'idle',
        x: 50,
        y: 95,
        sceneHeight: 0.12,
        scale: 1,
        zOrder: 15,
        actionCode: 'FEED_PETS',
        label: 'Миски',
      },
      {
        id: 'dining_table',
        entityName: 'dining_table',
        animation: 'idle',
        x: 50,
        y: 88,
        sceneHeight: 0.35,
        scale: 1,
        zOrder: 12,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 45,
        y: 92,
        scale: 1,
        zOrder: 50,
      },
    ],
  },

  dacha_yard: {
    id: 'dacha_yard',
    name: 'Двор на даче',
    locationAsset: 'dacha_yard',
    backgroundAnimation: 'idle',
    furniture: [
      {
        id: 'dog_house',
        entityName: 'dog_house',
        animation: 'idle',
        x: 15,
        y: 85,
        sceneHeight: 0.35,
        scale: 1,
        zOrder: 10,
        actionCode: 'WALK_DOG',
        label: 'Будка Сэма',
      },
      {
        id: 'hammock',
        entityName: 'hammock',
        animation: 'idle',
        x: 65,
        y: 80,
        sceneHeight: 0.30,
        scale: 1,
        zOrder: 10,
        actionCode: 'REST_AT_HOME',
        label: 'Гамак',
      },
      {
        id: 'campfire',
        entityName: 'campfire',
        animation: 'burning',
        x: 45,
        y: 92,
        sceneHeight: 0.20,
        scale: 1,
        zOrder: 15,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 50,
        y: 90,
        scale: 1,
        zOrder: 50,
      },
      {
        id: 'sam',
        entityName: 'sam',
        defaultAnimation: 'idle',
        x: 30,
        y: 92,
        scale: 1,
        zOrder: 45,
      },
    ],
  },

  parents_cottage: {
    id: 'parents_cottage',
    name: 'Дом родителей',
    locationAsset: 'parents_cottage',
    backgroundAnimation: 'idle',
    furniture: [
      {
        id: 'fireplace',
        entityName: 'fireplace',
        animation: 'burning',
        x: 25,
        y: 80,
        sceneHeight: 0.45,
        scale: 1,
        zOrder: 10,
      },
      {
        id: 'dining_table',
        entityName: 'dining_table',
        animation: 'idle',
        x: 60,
        y: 88,
        sceneHeight: 0.35,
        scale: 1,
        zOrder: 12,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 40,
        y: 92,
        scale: 1,
        zOrder: 50,
      },
      {
        id: 'alexander',
        entityName: 'alexander',
        defaultAnimation: 'idle',
        x: 60,
        y: 92,
        scale: 1,
        zOrder: 48,
      },
    ],
  },

  summer_camp: {
    id: 'summer_camp',
    name: 'Летний лагерь',
    locationAsset: 'summer_camp',
    backgroundAnimation: 'idle',
    furniture: [
      {
        id: 'tent',
        entityName: 'tent',
        animation: 'idle',
        x: 20,
        y: 80,
        sceneHeight: 0.40,
        scale: 1,
        zOrder: 10,
      },
      {
        id: 'campfire',
        entityName: 'campfire',
        animation: 'burning',
        x: 50,
        y: 90,
        sceneHeight: 0.20,
        scale: 1,
        zOrder: 15,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 50,
        y: 90,
        scale: 1,
        zOrder: 50,
      },
    ],
  },
};

export const getLocationConfig = (locationId: string): LocationConfig => {
  return LOCATIONS[locationId] ?? LOCATIONS['home_room']!;
};

export const getLocationForTimeSlot = (timeSlot: string): string => {
  switch (timeSlot) {
    case 'MORNING': return 'home_room';
    case 'DAY':     return 'home_room';
    case 'EVENING': return 'kitchen';
    case 'NIGHT':   return 'home_room';
    default:        return 'home_room';
  }
};
