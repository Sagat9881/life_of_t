/**
 * Location configuration — entities rendered via Canvas 2D API.
 *
 * ── COORDINATE SYSTEM (Canvas 2D) ──
 * x, y: percentage (0–100) of scene dimensions.
 *   x=0 is left edge, x=100 is right edge.
 *   y=0 is top edge, y=100 is bottom edge.
 * Entity anchor: center-X, bottom-Y (character feet / furniture base).
 *
 * ── SIZING ──
 * sceneHeight: percentage (0–100) of scene height this entity occupies.
 *   e.g. sceneHeight=38 means 38% of 480px = ~182px (adult human).
 * scale: optional multiplier on top of sceneHeight. Default 1.0.
 *
 * All rendering happens via Canvas 2D drawImage() — no DOM, no CSS transforms.
 */

import type { FurniturePlacement, CharacterSlot, LocationConfig } from '../types/location.types';

// Re-export so existing imports from 'config/locations' continue to work.
export type { FurniturePlacement, CharacterSlot, LocationConfig };

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
        x: 12, y: 72,
        sceneHeight: 45,
        scale: 1, zOrder: 8,
      },
      {
        id: 'bed',
        entityName: 'bed',
        animation: 'idle',
        x: 22, y: 95,
        sceneHeight: 28,
        scale: 1, zOrder: 12,
        actionCode: 'REST_AT_HOME',
        label: 'Кровать',
      },
      {
        id: 'computer',
        entityName: 'computer',
        animation: 'idle',
        x: 52, y: 90,
        sceneHeight: 35,
        scale: 1, zOrder: 10,
        actionCode: 'WORK_ON_PROJECT',
        label: 'Компьютер',
      },
      {
        id: 'phone',
        entityName: 'phone',
        animation: 'idle',
        x: 68, y: 95,
        sceneHeight: 8,
        scale: 1, zOrder: 15,
        actionCode: 'CALL_HUSBAND',
        label: 'Телефон',
      },
      {
        id: 'mirror',
        entityName: 'mirror',
        animation: 'idle',
        x: 78, y: 75,
        sceneHeight: 38,
        scale: 1, zOrder: 10,
        actionCode: 'BEAUTY_ROUTINE',
        label: 'Зеркало',
      },
      {
        id: 'cat_tree',
        entityName: 'cat_tree',
        animation: 'idle',
        x: 92, y: 82,
        sceneHeight: 42,
        scale: 1, zOrder: 8,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 38, y: 92,
        sceneHeight: 38,
        scale: 1, zOrder: 50,
      },
      {
        id: 'alexander',
        entityName: 'alexander',
        defaultAnimation: 'idle',
        x: 56, y: 92,
        sceneHeight: 40,
        scale: 1, zOrder: 48,
      },
      {
        id: 'klop',
        entityName: 'klop',
        defaultAnimation: 'idle',
        x: 88, y: 95,
        sceneHeight: 18,
        scale: 1, zOrder: 52,
      },
      {
        id: 'garfield',
        entityName: 'garfield',
        defaultAnimation: 'idle',
        x: 30, y: 96,
        sceneHeight: 16,
        scale: 1, zOrder: 52,
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
        x: 20, y: 88,
        sceneHeight: 22,
        scale: 1, zOrder: 10,
        actionCode: 'COOK_FOOD',
        label: 'Плита',
      },
      {
        id: 'fridge',
        entityName: 'fridge',
        animation: 'idle',
        x: 78, y: 82,
        sceneHeight: 42,
        scale: 1, zOrder: 10,
        actionCode: 'EAT_FOOD',
        label: 'Холодильник',
      },
      {
        id: 'pet_bowl',
        entityName: 'pet_bowl',
        animation: 'idle',
        x: 50, y: 97,
        sceneHeight: 6,
        scale: 1, zOrder: 15,
        actionCode: 'FEED_PETS',
        label: 'Миски',
      },
      {
        id: 'dining_table',
        entityName: 'dining_table',
        animation: 'idle',
        x: 50, y: 90,
        sceneHeight: 20,
        scale: 1, zOrder: 12,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 45, y: 92,
        sceneHeight: 38,
        scale: 1, zOrder: 50,
      },
      {
        id: 'klop',
        entityName: 'klop',
        defaultAnimation: 'idle',
        x: 60, y: 96,
        sceneHeight: 18,
        scale: 1, zOrder: 52,
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
        x: 15, y: 88,
        sceneHeight: 25,
        scale: 1, zOrder: 10,
        actionCode: 'WALK_DOG',
        label: 'Будка Сэма',
      },
      {
        id: 'hammock',
        entityName: 'hammock',
        animation: 'idle',
        x: 65, y: 82,
        sceneHeight: 22,
        scale: 1, zOrder: 10,
        actionCode: 'REST_AT_HOME',
        label: 'Гамак',
      },
      {
        id: 'campfire',
        entityName: 'campfire',
        animation: 'burning',
        x: 45, y: 95,
        sceneHeight: 14,
        scale: 1, zOrder: 15,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 50, y: 90,
        sceneHeight: 38,
        scale: 1, zOrder: 50,
      },
      {
        id: 'sam',
        entityName: 'sam',
        defaultAnimation: 'idle',
        x: 25, y: 92,
        sceneHeight: 40,
        scale: 1, zOrder: 45,
      },
      {
        id: 'duke',
        entityName: 'duke',
        defaultAnimation: 'idle',
        x: 70, y: 93,
        sceneHeight: 20,
        scale: 1, zOrder: 46,
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
        x: 25, y: 82,
        sceneHeight: 35,
        scale: 1, zOrder: 10,
      },
      {
        id: 'dining_table',
        entityName: 'dining_table',
        animation: 'idle',
        x: 60, y: 90,
        sceneHeight: 20,
        scale: 1, zOrder: 12,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 40, y: 92,
        sceneHeight: 38,
        scale: 1, zOrder: 50,
      },
      {
        id: 'alexander',
        entityName: 'alexander',
        defaultAnimation: 'idle',
        x: 60, y: 92,
        sceneHeight: 40,
        scale: 1, zOrder: 48,
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
        x: 20, y: 82,
        sceneHeight: 32,
        scale: 1, zOrder: 10,
      },
      {
        id: 'campfire',
        entityName: 'campfire',
        animation: 'burning',
        x: 50, y: 92,
        sceneHeight: 14,
        scale: 1, zOrder: 15,
      },
    ],
    characters: [
      {
        id: 'tanya',
        entityName: 'tanya',
        defaultAnimation: 'idle',
        x: 50, y: 90,
        sceneHeight: 38,
        scale: 1, zOrder: 50,
      },
    ],
  },
};

export const getLocationConfig = (locationId: string): LocationConfig =>
  LOCATIONS[locationId] ?? LOCATIONS['home_room']!;

export const getLocationForTimeSlot = (timeSlot: string): string => {
  switch (timeSlot) {
    case 'MORNING': return 'home_room';
    case 'DAY':     return 'home_room';
    case 'EVENING': return 'kitchen';
    case 'NIGHT':   return 'home_room';
    default:        return 'home_room';
  }
};
