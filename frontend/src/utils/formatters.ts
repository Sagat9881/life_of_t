// Функции форматирования данных

import type { Stats, GameTime, TimeSlot } from '@/types/game';
import { TIME_SLOTS, STAT_NAMES } from './constants';

/**
 * Форматирование времени суток
 */
export const formatTimeSlot = (timeSlot: TimeSlot): string => {
  return TIME_SLOTS[timeSlot];
};

/**
 * Форматирование игрового времени
 */
export const formatGameTime = (time: GameTime): string => {
  return `День ${time.day}, ${formatTimeSlot(time.timeSlot)}`;
};

/**
 * Форматирование денег
 */
export const formatMoney = (amount: number): string => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
};

/**
 * Форматирование процента (0-100)
 */
export const formatPercent = (value: number): string => {
  return `${Math.round(value)}%`;
};

/**
 * Форматирование статы с именем
 */
export const formatStatWithName = (statKey: keyof Stats, value: number): string => {
  const name = STAT_NAMES[statKey];
  
  if (statKey === 'money') {
    return `${name}: ${formatMoney(value)}`;
  }
  
  return `${name}: ${value}`;
};

/**
 * Получение цвета для статы по значению
 */
export const getStatColor = (value: number): string => {
  if (value >= 70) return 'var(--color-success)';
  if (value >= 40) return 'var(--color-warning)';
  return 'var(--color-error)';
};

/**
 * Получение цвета для стресса (инвертированный)
 */
export const getStressColor = (stress: number): string => {
  if (stress <= 30) return 'var(--color-success)';
  if (stress <= 60) return 'var(--color-warning)';
  return 'var(--color-error)';
};

/**
 * Форматирование времени действия
 */
export const formatActionTime = (hours: number): string => {
  if (hours < 1) {
    const minutes = Math.round(hours * 60);
    return `${minutes} мин`;
  }
  
  const wholeHours = Math.floor(hours);
  const minutes = Math.round((hours - wholeHours) * 60);
  
  if (minutes === 0) {
    return `${wholeHours} ч`;
  }
  
  return `${wholeHours} ч ${minutes} мин`;
};

/**
 * Сокращение длинного текста
 */
export const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return `${text.substring(0, maxLength - 3)}...`;
};

/**
 * Форматирование прогресса квеста
 */
export const formatQuestProgress = (progress: number): string => {
  return `${Math.round(progress)}%`;
};

/**
 * Получение emoji для настроения
 */
export const getMoodEmoji = (mood: number): string => {
  if (mood >= 80) return '😊';
  if (mood >= 60) return '🙂';
  if (mood >= 40) return '😐';
  if (mood >= 20) return '😔';
  return '😢';
};

/**
 * Получение emoji для энергии
 */
export const getEnergyEmoji = (energy: number): string => {
  if (energy >= 80) return '⚡';
  if (energy >= 60) return '🔋';
  if (energy >= 40) return '🪫';
  if (energy >= 20) return '⚠️';
  return '🔴';
};

/**
 * Форматирование изменения статы (для отображения в уведомлениях)
 */
export const formatStatChange = (statKey: keyof Stats, change: number): string => {
  const name = STAT_NAMES[statKey];
  const sign = change > 0 ? '+' : '';
  
  if (statKey === 'money') {
    return `${name}: ${sign}${formatMoney(Math.abs(change))}`;
  }
  
  return `${name}: ${sign}${change}`;
};
