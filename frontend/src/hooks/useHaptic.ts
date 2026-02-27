import { useCallback } from 'react';
import { useTelegram } from './useTelegram';

type ImpactStyle = 'light' | 'medium' | 'heavy' | 'rigid' | 'soft';
type NotificationType = 'error' | 'success' | 'warning';

interface UseHapticResult {
  impact: (style?: ImpactStyle) => void;
  notification: (type: NotificationType) => void;
  selection: () => void;
}

/**
 * Хук для haptic feedback в Telegram
 */
export const useHaptic = (): UseHapticResult => {
  const { webApp } = useTelegram();

  const impact = useCallback(
    (style: ImpactStyle = 'medium') => {
      webApp?.HapticFeedback.impactOccurred(style);
    },
    [webApp]
  );

  const notification = useCallback(
    (type: NotificationType) => {
      webApp?.HapticFeedback.notificationOccurred(type);
    },
    [webApp]
  );

  const selection = useCallback(() => {
    webApp?.HapticFeedback.selectionChanged();
  }, [webApp]);

  return {
    impact,
    notification,
    selection,
  };
};
