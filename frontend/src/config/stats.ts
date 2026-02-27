import type { StatKey } from '../types/game';

export interface StatConfig {
  name: string;
  icon: string;
  max: number;
  inverted: boolean; // true для стресса (чем меньше, тем лучше)
}

export const STAT_CONFIG: Record<StatKey, StatConfig> = {
  energy: {
    name: 'Энергия',
    icon: '⚡',
    max: 100,
    inverted: false,
  },
  health: {
    name: 'Здоровье',
    icon: '❤️',
    max: 100,
    inverted: false,
  },
  stress: {
    name: 'Стресс',
    icon: '😰',
    max: 100,
    inverted: true, // Чем меньше стресса, тем лучше
  },
  mood: {
    name: 'Настроение',
    icon: '😊',
    max: 100,
    inverted: false,
  },
  money: {
    name: 'Деньги',
    icon: '💰',
    max: 999999,
    inverted: false,
  },
  selfEsteem: {
    name: 'Самооценка',
    icon: '💪',
    max: 100,
    inverted: false,
  },
};
