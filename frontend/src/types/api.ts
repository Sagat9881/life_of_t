// Типы для API запросов и ответов

import type { GameState } from './game';

// Request DTOs
export interface StartSessionRequest {
  telegramUserId: number;
}

export interface ExecuteActionRequest {
  telegramUserId: number;
  actionCode: string;
}

export interface ChooseConflictTacticRequest {
  telegramUserId: number;
  conflictId: string;
  tacticCode: string;
}

export interface ChooseEventOptionRequest {
  telegramUserId: number;
  eventId: string;
  optionCode: string;
}

// Response типы
export interface ApiResponse<T> {
  data: T;
  error?: ApiError;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

// Типы ответов от API
export type GameStateResponse = GameState;

export type StartSessionResponse = GameState;

export type ExecuteActionResponse = GameState;

export type ChooseConflictTacticResponse = GameState;

export type ChooseEventOptionResponse = GameState;

// HTTP клиент конфиг
export interface ApiConfig {
  baseUrl: string;
  timeout: number;
  headers: Record<string, string>;
}

// API методы
export interface GameApi {
  startSession: (request: StartSessionRequest) => Promise<StartSessionResponse>;
  getState: (telegramUserId: number) => Promise<GameStateResponse>;
  executeAction: (request: ExecuteActionRequest) => Promise<ExecuteActionResponse>;
  chooseConflictTactic: (request: ChooseConflictTacticRequest) => Promise<ChooseConflictTacticResponse>;
  chooseEventOption: (request: ChooseEventOptionRequest) => Promise<ChooseEventOptionResponse>;
}
