import type { GameTime, TimeSlot } from '@/types/game';

const TIME_SLOT_LABELS: Record<TimeSlot, string> = {
  MORNING: 'Утро',
  DAY: 'День',
  EVENING: 'Вечер',
  NIGHT: 'Ночь',
} as const;

export function formatTime(time: GameTime): string {
  const slotLabel = TIME_SLOT_LABELS[time.timeSlot];
  return `День ${time.day}, ${time.hour}:00 (${slotLabel})`;
}

export function formatMoney(amount: number): string {
  return `${amount.toLocaleString('ru-RU')} ₽`;
}

export function formatStat(value: number, max: number): string {
  return `${Math.round(value)}/${max}`;
}

// Цвета для статов
export function getStatColor(value: number, max: number): string {
  const percentage = (value / max) * 100;
  
  if (percentage >= 70) return 'var(--color-success)';
  if (percentage >= 40) return 'var(--color-warning)';
  return 'var(--color-danger)';
}

// Цвет для стресса (инвертированный: чем меньше, тем лучше)
export function getStressColor(value: number, max: number): string {
  const percentage = (value / max) * 100;
  
  if (percentage <= 30) return 'var(--color-success)';
  if (percentage <= 60) return 'var(--color-warning)';
  return 'var(--color-danger)';
}
