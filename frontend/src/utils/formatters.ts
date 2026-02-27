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
