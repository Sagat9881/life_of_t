import { useCallback, useState } from 'react';
import { useGameStore } from '@/store/gameStore';
import { gameApi } from '../client';
import type { ActionCode, TacticCode } from '@/types/game';
import { useHaptic } from '@/hooks/useHaptic';

interface UseActionsResult {
  executeAction: (actionCode: ActionCode) => Promise<void>;
  chooseConflictTactic: (conflictId: string, tacticCode: TacticCode) => Promise<void>;
  chooseEventOption: (eventId: string, optionCode: string) => Promise<void>;
  isExecuting: boolean;
  error: string | null;
}

/**
 * Хук для выполнения игровых действий
 */
export const useActions = (): UseActionsResult => {
  const { notification, impact } = useHaptic();
  const { setGameState, setLoading } = useGameStore();
  const telegramUserId = useGameStore((state) => state.telegramUserId);
  const [isExecuting, setIsExecuting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const executeAction = useCallback(
    async (actionCode: ActionCode) => {
      if (!telegramUserId) {
        setError('ID пользователя не найден');
        notification('error');
        return;
      }

      setIsExecuting(true);
      setError(null);
      setLoading(true);
      impact('medium');

      try {
        const newState = await gameApi.executeAction({
          telegramUserId,
          actionCode,
        });

        setGameState(newState);
        notification('success');
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Не удалось выполнить действие';
        setError(errorMessage);
        notification('error');
      } finally {
        setIsExecuting(false);
        setLoading(false);
      }
    },
    [telegramUserId, setGameState, setLoading, notification, impact]
  );

  const chooseConflictTactic = useCallback(
    async (conflictId: string, tacticCode: TacticCode) => {
      if (!telegramUserId) {
        setError('ID пользователя не найден');
        notification('error');
        return;
      }

      setIsExecuting(true);
      setError(null);
      setLoading(true);
      impact('medium');

      try {
        const newState = await gameApi.chooseConflictTactic({
          telegramUserId,
          conflictId,
          tacticCode,
        });

        setGameState(newState);
        notification('success');
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Не удалось разрешить конфликт';
        setError(errorMessage);
        notification('error');
      } finally {
        setIsExecuting(false);
        setLoading(false);
      }
    },
    [telegramUserId, setGameState, setLoading, notification, impact]
  );

  const chooseEventOption = useCallback(
    async (eventId: string, optionCode: string) => {
      if (!telegramUserId) {
        setError('ID пользователя не найден');
        notification('error');
        return;
      }

      setIsExecuting(true);
      setError(null);
      setLoading(true);
      impact('light');

      try {
        const newState = await gameApi.chooseEventOption({
          telegramUserId,
          eventId,
          optionCode,
        });

        setGameState(newState);
        notification('success');
      } catch (err) {
        const errorMessage =
          err instanceof Error ? err.message : 'Не удалось выбрать вариант';
        setError(errorMessage);
        notification('error');
      } finally {
        setIsExecuting(false);
        setLoading(false);
      }
    },
    [telegramUserId, setGameState, setLoading, notification, impact]
  );

  return {
    executeAction,
    chooseConflictTactic,
    chooseEventOption,
    isExecuting,
    error,
  };
};
