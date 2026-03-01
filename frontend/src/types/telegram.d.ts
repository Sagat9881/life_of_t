/**
 * Telegram WebApp global type declarations
 * This file prevents type conflicts across the project
 */

interface TelegramHapticFeedback {
  impactOccurred: (style: 'light' | 'medium' | 'heavy' | 'rigid' | 'soft') => void;
  notificationOccurred: (type: 'error' | 'success' | 'warning') => void;
  selectionChanged: () => void;
}

interface TelegramWebApp {
  HapticFeedback?: TelegramHapticFeedback;
  ready?: () => void;
  expand?: () => void;
  close?: () => void;
}

interface Window {
  Telegram?: {
    WebApp?: TelegramWebApp;
  };
}

export {};