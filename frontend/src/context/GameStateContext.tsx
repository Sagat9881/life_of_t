/**
 * GameStateContext - Global game state management
 * Provides stats, time, money, location to all components
 */

import React, { createContext, useContext, useState, ReactNode } from 'react';
import { GameState } from '../types/SceneModel';

interface GameStateContextType extends GameState {
  updateStats: (stats: Partial<GameState['stats']>) => void;
  updateTime: (time: Partial<GameState['time']>) => void;
  updateMoney: (amount: number) => void;
  setLocation: (location: GameState['location']) => void;
  updateCharacter: (character: Partial<GameState['character']>) => void;
  updateObjectState: (objectId: string, state: string) => void;
}

const GameStateContext = createContext<GameStateContextType | undefined>(undefined);

const initialState: GameState = {
  location: 'room',
  character: {
    position: { x: 500, y: 400, zIndex: 4 },
    state: 'idle',
    variant: 'idle-neutral',
    emotion: 'neutral',
  },
  stats: {
    energy: 70,
    happiness: 80,
    health: 90,
  },
  time: {
    current: '09:00',
    date: '01 Мар',
    weekday: 'Вс',
    dayNumber: 1,
  },
  money: 1500,
  objectStates: {
    bed: 'neat',
    computer: 'off',
    phone: 'idle',
  },
};

export const GameStateProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [state, setState] = useState<GameState>(initialState);

  const updateStats = (newStats: Partial<GameState['stats']>) => {
    setState((prev) => ({
      ...prev,
      stats: { ...prev.stats, ...newStats },
    }));
  };

  const updateTime = (newTime: Partial<GameState['time']>) => {
    setState((prev) => ({
      ...prev,
      time: { ...prev.time, ...newTime },
    }));
  };

  const updateMoney = (amount: number) => {
    setState((prev) => ({
      ...prev,
      money: prev.money + amount,
    }));
  };

  const setLocation = (location: GameState['location']) => {
    setState((prev) => ({
      ...prev,
      location,
    }));
  };

  const updateCharacter = (character: Partial<GameState['character']>) => {
    setState((prev) => ({
      ...prev,
      character: { ...prev.character, ...character },
    }));
  };

  const updateObjectState = (objectId: string, state: string) => {
    setState((prev) => ({
      ...prev,
      objectStates: {
        ...prev.objectStates,
        [objectId]: state,
      },
    }));
  };

  return (
    <GameStateContext.Provider
      value={{
        ...state,
        updateStats,
        updateTime,
        updateMoney,
        setLocation,
        updateCharacter,
        updateObjectState,
      }}
    >
      {children}
    </GameStateContext.Provider>
  );
};

export const useGameState = () => {
  const context = useContext(GameStateContext);
  if (!context) {
    throw new Error('useGameState must be used within GameStateProvider');
  }
  return context;
};