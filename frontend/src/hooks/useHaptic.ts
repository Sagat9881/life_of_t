/**
 * useHaptic - Telegram WebApp Haptic Feedback hook
 * Based on BottomNavigation.xml haptic specifications
 */

import { useCallback } from 'react';

interface TelegramWebApp {
  HapticFeedback?: {
    impactOccurred: (style: 'light' | 'medium' | 'heavy' | 'rigid' | 'soft') => void;
    notificationOccurred: (type: 'error' | 'success' | 'warning') => void;
    selectionChanged: () => void;
  };
}

declare global {
  interface Window {
    Telegram?: {
      WebApp?: TelegramWebApp;
    };
  }
}

export const useHaptic = () => {
  const haptic = window.Telegram?.WebApp?.HapticFeedback;

  const light = useCallback(() => {
    haptic?.impactOccurred('light');
  }, [haptic]);

  const medium = useCallback(() => {
    haptic?.impactOccurred('medium');
  }, [haptic]);

  const heavy = useCallback(() => {
    haptic?.impactOccurred('heavy');
  }, [haptic]);

  const success = useCallback(() => {
    haptic?.notificationOccurred('success');
  }, [haptic]);

  const error = useCallback(() => {
    haptic?.notificationOccurred('error');
  }, [haptic]);

  const warning = useCallback(() => {
    haptic?.notificationOccurred('warning');
  }, [haptic]);

  const selectionChanged = useCallback(() => {
    haptic?.selectionChanged();
  }, [haptic]);

  return {
    light,
    medium,
    heavy,
    success,
    error,
    warning,
    selectionChanged,
  };
};