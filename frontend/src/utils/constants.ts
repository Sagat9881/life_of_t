// Константы приложения

// API
// Используем пустую строку для относительных URL (работает с любым портом)
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';
export const API_TIMEOUT = 10000; // 10 секунд

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
  
  // Дополнительные
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

// Названия действий
export const ACTION_NAMES = {
  WORK: 'Работа',
  DATE_WITH_HUSBAND: 'Свидание с мужем',
  FEED_PETS: 'Покормить питомцев',
  PLAY_WITH_PETS: 'Поиграть с питомцами',
  WALK_PETS: 'Выгулять питомцев',
  VISIT_FATHER: 'Навестить отца',
  REST: 'Отдохнуть',
  SOCIAL_MEDIA: 'Соцсети',
} as const;

// Названия тактик
export const TACTIC_NAMES = {
  SURRENDER: 'Уступить',
  ASSERT: 'Настоять на своём',
  COMPROMISE: 'Компромисс',
  AVOID: 'Избежать',
  LISTEN_AND_UNDERSTAND: 'Выслушать и понять',
  USE_HUMOR: 'Пошутить',
  LOGICAL_ARGUMENT: 'Логический аргумент',
  EMOTIONAL_APPEAL: 'Эмоциональный призыв',
  SET_BOUNDARIES: 'Установить границы',
} as const;

// Названия статов
export const STAT_NAMES = {
  energy: 'Энергия',
  health: 'Здоровье',
  stress: 'Стресс',
  mood: 'Настроение',
  money: 'Деньги',
  selfEsteem: 'Самооценка',
} as const;

// Иконки для статов (используем Lucide React)
export const STAT_ICONS = {
  energy: 'Zap',
  health: 'Heart',
  stress: 'AlertCircle',
  mood: 'Smile',
  money: 'DollarSign',
  selfEsteem: 'Award',
} as const;

// Типы питомцев
export const PET_NAMES = {
  GARFIELD: 'Гарфилд',
  SAM: 'Сэм',
} as const;

// Типы NPC
export const NPC_NAMES = {
  HUSBAND: 'Муж',
  FATHER: 'Отец',
} as const;

// Лимиты
export const LIMITS = {
  maxEnergy: 100,
  maxHealth: 100,
  minMoneyForAction: 0,
  questProgressStep: 10,
} as const;

// Размеры бандла
export const BUNDLE_SIZE_LIMIT = 200 * 1024; // 200KB gzipped
