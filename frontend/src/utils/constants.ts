// Константы приложения

// API
// Используем пустую строку для относительных URL (работает с любым портом)
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
export const API_TIMEOUT = 10000; // 10 секунд

// Assets — путь к сгенерированным ассетам.
// Generator (NamingSpec) outputs to: {outputDir}/assets/{entityType}/{entityName}/
// Spring Boot serves from: /assets/{entityType}/{entityName}/
export const ASSETS_BASE_URL = import.meta.env.VITE_ASSETS_BASE_URL || '/assets';

// Статы
export const MIN_STAT = 0;
export const MAX_STAT = 100;

// Цвета (из PROJECT_CONTEXT.md)
export const COLORS = {
  primary: '#FF6B9D',      // розовый для романтики
  secondary: '#4ECDC4',    // мятный для свежести
  accent: '#FFE66D',       // жёлтый для энергии
  background: '#F7F7F7',   // светло-серый
  text: '#2C3E50',         // тёмно-синий
  success: '#4CAF50',
  warning: '#FF9800',
  error: '#F44336',
  info: '#2196F3',
} as const;

// Время анимаций
export const ANIMATION_DURATION = {
  fast: 200,
  normal: 300,
  slow: 500,
} as const;

// Время суток
export const TIME_SLOTS = {
  MORNING: 'Утро',
  DAY: 'День',
  EVENING: 'Вечер',
  NIGHT: 'Ночь',
} as const;

// Лимиты
export const LIMITS = {
  maxEnergy: 100,
  maxHealth: 100,
  minMoneyForAction: 0,
  questProgressStep: 10,
} as const;
