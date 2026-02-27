import { useEffect, useState } from 'react';

interface HapticFeedback {
  impactOccurred: (style: 'light' | 'medium' | 'heavy') => void;
  notificationOccurred: (type: 'error' | 'success' | 'warning') => void;
  selectionChanged: () => void;
}

export interface UseTelegramResult {
  webApp: any;
  user: any;
  hapticFeedback?: HapticFeedback | undefined;
}

export function useTelegram(): UseTelegramResult {
  const [webApp, setWebApp] = useState<any>(null);
  const [user, setUser] = useState<any>(null);

  useEffect(() => {
    // Проверяем наличие Telegram WebApp
    if (typeof window !== 'undefined' && (window as any).Telegram?.WebApp) {
      const tg = (window as any).Telegram.WebApp;
      setWebApp(tg);
      setUser(tg.initDataUnsafe?.user);

      // Расширяем WebApp
      tg.ready();
      tg.expand();
    }
  }, []);

  const hapticFeedback: HapticFeedback | undefined = webApp?.HapticFeedback ? {
    impactOccurred: (style: 'light' | 'medium' | 'heavy') => {
      webApp.HapticFeedback.impactOccurred(style);
    },
    notificationOccurred: (type: 'error' | 'success' | 'warning') => {
      webApp.HapticFeedback.notificationOccurred(type);
    },
    selectionChanged: () => {
      webApp.HapticFeedback.selectionChanged();
    },
  } : undefined;

  return {
    webApp,
    user,
    hapticFeedback,
  };
}
