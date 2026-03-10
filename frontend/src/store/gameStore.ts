import { create } from 'zustand';
import { api } from '../services/api';
import type {
  Player, GameTime, ActionOption, RelationshipView,
  PetView, QuestView, ConflictView, EventView,
  EndingView, ActionResultView, NpcActivityView, DomainEventView,
} from '../types/game';

interface GameStore {
  player: Player | null;
  time: GameTime | null;
  availableActions: ActionOption[];
  relationships: RelationshipView[];
  pets: PetView[];
  activeQuests: QuestView[];
  completedQuestIds: string[];
  activeConflicts: ConflictView[];
  currentEvent: EventView | null;
  ending: EndingView | null;
  lastActionResult: ActionResultView | null;
  npcActivities: NpcActivityView[];
  domainEvents: DomainEventView[];
  isLoading: boolean;
  error: string | null;
  fetchGameState: () => Promise<void>;
  executeAction: (actionCode: string) => Promise<void>;
  selectTactic: (conflictId: string, tacticCode: string) => Promise<void>;
  selectChoice: (eventId: string, optionCode: string) => Promise<void>;
  cancelConflict: () => void;
  cancelEvent: () => void;
  reset: () => void;
}

const initialState = {
  player: null, time: null, availableActions: [], relationships: [],
  pets: [], activeQuests: [], completedQuestIds: [], activeConflicts: [],
  currentEvent: null, ending: null, lastActionResult: null,
  npcActivities: [], domainEvents: [],
  isLoading: false, error: null,
};

function applyState(state: Record<string, unknown>) {
  return {
    player: (state.player as Player) ?? null,
    time: (state.time as GameTime) ?? null,
    availableActions: (state.availableActions as ActionOption[]) ?? [],
    relationships: (state.relationships as RelationshipView[]) ?? [],
    pets: (state.pets as PetView[]) ?? [],
    activeQuests: (state.activeQuests as QuestView[]) ?? [],
    completedQuestIds: (state.completedQuestIds as string[]) ?? [],
    activeConflicts: (state.activeConflicts as ConflictView[]) ?? [],
    currentEvent: (state.currentEvent as EventView) ?? null,
    ending: (state.ending as EndingView) ?? null,
    lastActionResult: (state.lastActionResult as ActionResultView) ?? null,
    npcActivities: (state.npcActivities as NpcActivityView[]) ?? [],
    domainEvents: (state.domainEvents as DomainEventView[]) ?? [],
    isLoading: false, error: null,
  };
}

export const useGameStore = create<GameStore>((set) => ({
  ...initialState,
  fetchGameState: async () => {
    set({ isLoading: true, error: null });
    try { const state = await api.getGameState(); set(applyState(state as unknown as Record<string, unknown>)); }
    catch (error) { set({ error: error instanceof Error ? error.message : 'Не удалось загрузить данные', isLoading: false }); }
  },
  executeAction: async (actionCode: string) => {
    set({ isLoading: true, error: null });
    try { const result = await api.executeAction(actionCode); set(applyState(result as unknown as Record<string, unknown>)); }
    catch (error) { set({ error: error instanceof Error ? error.message : 'Не удалось выполнить действие', isLoading: false }); }
  },
  selectTactic: async (conflictId: string, tacticCode: string) => {
    set({ isLoading: true, error: null });
    try { const result = await api.resolveTactic(conflictId, tacticCode); set(applyState(result as unknown as Record<string, unknown>)); }
    catch (error) { set({ error: error instanceof Error ? error.message : 'Не удалось разрешить конфликт', isLoading: false }); }
  },
  selectChoice: async (eventId: string, optionCode: string) => {
    set({ isLoading: true, error: null });
    try { const result = await api.selectChoice(eventId, optionCode); set(applyState(result as unknown as Record<string, unknown>)); }
    catch (error) { set({ error: error instanceof Error ? error.message : 'Не удалось выбрать вариант', isLoading: false }); }
  },
  cancelConflict: () => set({ activeConflicts: [] }),
  cancelEvent: () => set({ currentEvent: null }),
  reset: () => set(initialState),
}));
