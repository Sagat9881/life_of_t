// API request/response types — aligned with backend DTOs

import type { GameState } from './game';

export interface StartSessionRequest {
  telegramUserId: string;
}

export interface ExecuteActionRequest {
  telegramUserId: string;
  actionCode: string;
}

export interface ChooseConflictTacticRequest {
  telegramUserId: string;
  conflictId: string;
  tacticCode: string;
}

export interface ChooseEventOptionRequest {
  telegramUserId: string;
  eventId: string;
  optionCode: string;
}

export interface EndDayRequest {
  telegramUserId: string;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

/** Backend returns GameState (GameStateView) directly */
export type GameStateResponse = GameState;
export type StartSessionResponse = GameState;
export type ExecuteActionResponse = GameState;
export type ChooseConflictTacticResponse = GameState;
export type ChooseEventOptionResponse = GameState;
export type EndDayResponse = GameState;

export interface ApiConfig {
  baseUrl: string;
  timeout: number;
  headers: Record<string, string>;
}

export interface GameApi {
  startSession: (request: StartSessionRequest) => Promise<StartSessionResponse>;
  getState: (telegramUserId: string) => Promise<GameStateResponse>;
  executeAction: (request: ExecuteActionRequest) => Promise<ExecuteActionResponse>;
  chooseConflictTactic: (request: ChooseConflictTacticRequest) => Promise<ChooseConflictTacticResponse>;
  chooseEventOption: (request: ChooseEventOptionRequest) => Promise<ChooseEventOptionResponse>;
  endDay: (request: EndDayRequest) => Promise<EndDayResponse>;
}
