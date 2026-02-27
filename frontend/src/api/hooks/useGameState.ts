import { useEffect, useCallback } from 'react';
import { useGameStore } from '@/store/gameStore';
import { gameApi } from '../client';
import { useTelegram } from '@/hooks/useTelegram';

interface UseGameStateResult {
  isLoading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

/**
 * Хук для загрузки и управления игровым состоянием
 */
export const useGameState = (): UseGameStateResult => {
  const { user } = useTelegram();
  const { setGameState, setLoading, setError, setTelegramUserId } = useGameStore();
  const isLoading = useGameStore((state) => state.isLoading);
  const error = useGameStore((state) => state.error);

  const loadGameState = useCallback(async () => {
    if (!user?.id) {
      setError('Не удалось получить ID пользователя Telegram');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // Пробуем загрузить существующую сессию
      const state = await gameApi.getState(user.id);
      setGameState(state);
      setTelegramUserId(user.id);
    } catch (err) {
      // Если сессии нет, создаём новую
      try {
        const state = await gameApi.startSession({ telegramUserId: user.id });
        setGameState(state);
        setTelegramUserId(user.id);
      } catch (startErr) {
        const errorMessage =
          startErr instanceof Error ? startErr.message : 'Не удалось загрузить игру';
        setError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  }, [user?.id, setGameState, setLoading, setError, setTelegramUserId]);

  const refetch = useCallback(async () => {
    await loadGameState();
  }, [loadGameState]);

  // Загружаем состояние при монтировании
  useEffect(() => {
    void loadGameState();
  }, [loadGameState]);

  return {
    isLoading,
    error,
    refetch,
  };
};
