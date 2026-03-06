/**
 * Location configuration — maps location IDs to their visual layout.
 * Defines background asset, furniture placements, and character spawn points.
 *
 * ── COORDINATE SYSTEM ──
 * All coordinates are in PERCENTAGE (0-100) relative to the scene viewport.
 * Scale values are in NATIVE pixel scale (not CSS pixels).
 *
 * PixelScene renders at native resolution (480×270) and auto-upscales.
 * A character at scale=1 shows at their real pixel size (e.g. 32×48).
 * Scale=5 means 160×240 in the 480×270 native scene = ~55% height.
 *
 * For human characters: scale=5 → occupies ~55% of scene height. Good default.
 * For pets: scale=3 → occupies ~30% of scene height.
 * For furniture: scale=2-3 → depends on furniture size.
 */

export interface FurniturePlacement {
  readonly id: string;
  readonly entityName: string;
  readonly animation: string;
  readonly x: number;
  readonly y: number;
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
    backgroundAnimation: 'ambient',
    furniture: [
      {
        id: 'bed',
        entityName: 'bed',
        animation: 'idle',
        x: 8,
        y: 60,
        scale: 3,
        zOrder: 10,
        actionCode: 'REST_AT_HOME',
        label: 'Кровать',
      },
      {
        id: 'computer',
        entityName: 'computer',
        animation: 'idle',
        x: 52,
        y: 55,
        scale: 2,
        zOrder: 10,
        actionCode: 'WORK_ON_PROJECT',
        label: 'Компьютер',
      },
      {
        id: 'phone',
        entityName: 'phone',
        animation: 'idle',
        x: 22,
        y: 80,
        scale: 2,
        zOrder: 15,
        actionCode: 'CALL_HUSBAND',
        label: 'Телефон',
      },
      {
        id: 'mirror',
        entityName: 'mirror',
        animation: 'idle',
        x: 75,
        y: 60,
        scale: 2,
        zOrder: 10,
        actionCode: 'BEAUTY_ROUTINE',
        label: 'Зеркало',
      },
      {
        id: 'cat_tree',
        entityName: 'cat_tree',
        animation: 'idle',
        x: 90,
        y: 55,
        scale: 2,
        zOrder: 8,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 30,
        y: 88,
        scale: 5,
        zOrder: 50,
      },
      {
        id: 'alexander',
        entityName: 'alexander',
        defaultAnimation: 'idle',
        x: 55,
        y: 88,
        scale: 5,
        zOrder: 48,
      },
    ],
  },

  kitchen: {
    id: 'kitchen',
    name: 'Кухня',
    locationAsset: 'kitchen',
    backgroundAnimation: 'ambient',
    furniture: [
      {
        id: 'stove',
        entityName: 'stove',
        animation: 'idle',
        x: 25,
        y: 55,
        scale: 3,
        zOrder: 10,
        actionCode: 'COOK_FOOD',
        label: 'Плита',
      },
      {
        id: 'fridge',
        entityName: 'fridge',
        animation: 'idle',
        x: 75,
        y: 50,
        scale: 3,
        zOrder: 10,
        actionCode: 'EAT_FOOD',
        label: 'Холодильник',
      },
      {
        id: 'pet_bowl',
        entityName: 'pet_bowl',
        animation: 'idle',
        x: 50,
        y: 80,
        scale: 2,
        zOrder: 15,
        actionCode: 'FEED_PETS',
        label: 'Миски',
      },
      {
        id: 'dining_table',
        entityName: 'dining_table',
        animation: 'idle',
        x: 50,
        y: 60,
        scale: 3,
        zOrder: 12,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 45,
        y: 88,
        scale: 5,
        zOrder: 50,
      },
    ],
  },

  dacha_yard: {
    id: 'dacha_yard',
    name: 'Двор на даче',
    locationAsset: 'dacha_yard',
    backgroundAnimation: 'ambient',
    furniture: [
      {
        id: 'dog_house',
        entityName: 'dog_house',
        animation: 'idle',
        x: 20,
        y: 70,
        scale: 3,
        zOrder: 10,
        actionCode: 'WALK_DOG',
        label: 'Будка Сэма',
      },
      {
        id: 'hammock',
        entityName: 'hammock',
        animation: 'idle',
        x: 70,
        y: 55,
        scale: 3,
        zOrder: 10,
        actionCode: 'REST_AT_HOME',
        label: 'Гамак',
      },
      {
        id: 'campfire',
        entityName: 'campfire',
        animation: 'burning',
        x: 50,
        y: 80,
        scale: 2,
        zOrder: 15,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 50,
        y: 88,
        scale: 5,
        zOrder: 50,
      },
      {
        id: 'sam',
        entityName: 'sam',
        defaultAnimation: 'idle',
        x: 30,
        y: 90,
        scale: 3,
        zOrder: 45,
      },
    ],
  },

  parents_cottage: {
    id: 'parents_cottage',
    name: 'Дом родителей',
    locationAsset: 'parents_cottage',
    backgroundAnimation: 'ambient',
    furniture: [
      {
        id: 'fireplace',
        entityName: 'fireplace',
        animation: 'burning',
        x: 30,
        y: 55,
        scale: 3,
        zOrder: 10,
      },
      {
        id: 'dining_table',
        entityName: 'dining_table',
        animation: 'idle',
        x: 60,
        y: 60,
        scale: 3,
        zOrder: 12,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 40,
        y: 88,
        scale: 5,
        zOrder: 50,
      },
      {
        id: 'alexander',
        entityName: 'alexander',
        defaultAnimation: 'idle',
        x: 60,
        y: 88,
        scale: 5,
        zOrder: 48,
      },
    ],
  },

  summer_camp: {
    id: 'summer_camp',
    name: 'Летний лагерь',
    locationAsset: 'summer_camp',
    backgroundAnimation: 'ambient',
    furniture: [
      {
        id: 'tent',
        entityName: 'tent',
        animation: 'idle',
        x: 25,
        y: 55,
        scale: 3,
        zOrder: 10,
      },
      {
        id: 'campfire',
        entityName: 'campfire',
        animation: 'burning',
        x: 55,
        y: 75,
        scale: 2,
        zOrder: 15,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 50,
        y: 88,
        scale: 5,
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
    case 'MORNING':
      return 'home_room';
    case 'DAY':
      return 'home_room';
    case 'EVENING':
      return 'kitchen';
    case 'NIGHT':
      return 'home_room';
    default:
      return 'home_room';
  }
};
