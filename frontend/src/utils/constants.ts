// Константы приложения

// API
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
export const API_TIMEOUT = 10000;

// Assets
export const ASSETS_BASE_URL = import.meta.env.VITE_ASSETS_BASE_URL || '/assets';

// Статы
export const MIN_STAT = 0;
export const MAX_STAT = 100;

// Названия статов для UI
export const STAT_NAMES: Record<string, string> = {
  energy:      'Энергия',
  health:      'Здоровье',
  stress:      'Стресс',
  mood:        'Настроение',
  money:       'Деньги',
  selfEsteem:  'Самооценка',
};

// Цвета
export const COLORS = {
  primary:    '#FF6B9D',
  secondary:  '#4ECDC4',
  accent:     '#FFE66D',
  background: '#F7F7F7',
  text:       '#2C3E50',
  success:    '#4CAF50',
  warning:    '#FF9800',
  error:      '#F44336',
  info:       '#2196F3',
} as const;

// Время анимаций
export const ANIMATION_DURATION = {
  fast:   200,
  normal: 300,
  slow:   500,
} as const;

// Время суток
export const TIME_SLOTS = {
  MORNING: 'Утро',
  DAY:     'День',
  EVENING: 'Вечер',
  NIGHT:   'Ночь',
} as const;

// Лимиты
export const LIMITS = {
  maxEnergy:          100,
  maxHealth:          100,
  minMoneyForAction:  0,
  questProgressStep:  10,
} as const;
