/**
 * ContentStore — central storage for game content metadata.
 * 
 * Loaded once on app startup from /api/v1/content/* endpoints.
 * All UI components use this store instead of hardcoding game data.
 * 
 * Example usage:
 * ```tsx
 * function ActionsList() {
 *   const { actions } = useContentStore();
 *   const gameState = useGameState();
 *   
 *   return gameState.availableActions.map(opt => (
 *     <ActionButton
 *       key={opt.actionCode}
 *       title={actions[opt.actionCode].title}
 *       icon={actions[opt.actionCode].iconName}
 *     />
 *   ));
 * }
 * ```
 */

import { create } from 'zustand';

// ==================== Types ====================

export interface ContentVersion {
  version: string;
  updatedAt: string;
}

export interface ActionDef {
  code: string;
  title: string;
  description: string;
  tags: string[];
  energyCost: number;
  minEnergy: number;
  requiredSkills: Record<string, number>;
  requiredTags: string[];
  forbiddenTags: string[];
  statEffects: Record<string, number>;
  skillGains: Record<string, number>;
  moneyGain: number;
  durationMinutes: number;
  animationTrigger: string;
  iconName: string;
  availableTimeOfDay: string[];
  availableLocations: string[];
  potentialConflictTypes: string[];
  relatedQuestIds: string[];
}

export interface TacticDef {
  code: string;
  title: string;
  description: string;
  skillRequirements: Record<string, number>;
  stressReduction: number;
  relationshipEffects: Record<string, number>;
  skillGains: Record<string, number>;
  baseSuccessChance: number;
  skillSuccessModifiers: Record<string, number>;
}

export interface ConflictDef {
  type: string;
  title: string;
  description: string;
  tactics: TacticDef[];
  baseStressPoints: number;
}

export interface QuestStepDef {
  id: string;
  description: string;
  type: string;
  requiredActions: string[];
  requiredEventChoice: string | null;
  requiredConflictResolution: string | null;
  requiredSkillChecks: Record<string, number>;
}

export interface QuestDef {
  id: string;
  title: string;
  description: string;
  category: string;
  requiredTags: string[];
  minSkills: Record<string, number>;
  minDay: number;
  steps: QuestStepDef[];
  moneyReward: number;
  skillRewards: Record<string, number>;
  tagsGranted: string[];
  completionMessage: string;
}

export interface EventOptionDef {
  code: string;
  text: string;
  requiredSkills: Record<string, number>;
  requiredTags: string[];
  statEffects: Record<string, number>;
  relationshipEffects: Record<string, number>;
  skillGains: Record<string, number>;
  moneyEffect: number;
  tagsGranted: string[];
  tagsRemoved: string[];
  outcomeMessage: string;
}

export interface EventDef {
  id: string;
  title: string;
  description: string;
  category: string;
  requiredTags: string[];
  minSkills: Record<string, number>;
  minDay: number;
  maxDay: number | null;
  options: EventOptionDef[];
  priority: number;
  repeatable: boolean;
}

interface ContentState {
  // Content maps (keyed by code/id for O(1) lookup)
  actions: Record<string, ActionDef>;
  conflicts: Record<string, ConflictDef>;
  quests: Record<string, QuestDef>;
  events: Record<string, EventDef>;
  
  // Version tracking
  version: ContentVersion | null;
  
  // Loading state
  isLoaded: boolean;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  loadAllContent: () => Promise<void>;
  clearError: () => void;
}

// ==================== API Client ====================

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function fetchContent<T>(endpoint: string): Promise<T> {
  const response = await fetch(`${API_BASE}/api/v1/content/${endpoint}`);
  if (!response.ok) {
    throw new Error(`Failed to fetch ${endpoint}: ${response.statusText}`);
  }
  return response.json();
}

// ==================== Store ====================

export const useContentStore = create<ContentState>((set, get) => ({
  actions: {},
  conflicts: {},
  quests: {},
  events: {},
  version: null,
  isLoaded: false,
  isLoading: false,
  error: null,

  loadAllContent: async () => {
    // Prevent duplicate loads
    if (get().isLoading || get().isLoaded) {
      return;
    }

    set({ isLoading: true, error: null });

    try {
      // Fetch all content in parallel
      const [actionsRes, conflictsRes, questsRes, eventsRes] = await Promise.all([
        fetchContent<{ version: ContentVersion; actions: ActionDef[] }>('actions'),
        fetchContent<{ version: ContentVersion; conflicts: ConflictDef[] }>('conflicts'),
        fetchContent<{ version: ContentVersion; quests: QuestDef[] }>('quests'),
        fetchContent<{ version: ContentVersion; events: EventDef[] }>('events'),
      ]);

      // Normalize arrays to maps for fast lookup
      const actions = Object.fromEntries(
        actionsRes.actions.map(a => [a.code, a])
      );
      const conflicts = Object.fromEntries(
        conflictsRes.conflicts.map(c => [c.type, c])
      );
      const quests = Object.fromEntries(
        questsRes.quests.map(q => [q.id, q])
      );
      const events = Object.fromEntries(
        eventsRes.events.map(e => [e.id, e])
      );

      set({
        actions,
        conflicts,
        quests,
        events,
        version: actionsRes.version,
        isLoaded: true,
        isLoading: false,
      });

      console.log(
        `[ContentStore] Loaded content v${actionsRes.version.version}:`,
        { actions: Object.keys(actions).length,
          conflicts: Object.keys(conflicts).length,
          quests: Object.keys(quests).length,
          events: Object.keys(events).length }
      );
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Unknown error',
        isLoading: false,
      });
      console.error('[ContentStore] Failed to load content:', error);
    }
  },

  clearError: () => set({ error: null }),
}));

// ==================== Selectors ====================

/**
 * Get action definition by code.
 * Returns undefined if not found (safe for optional rendering).
 */
export const selectAction = (code: string) => (state: ContentState) =>
  state.actions[code];

/**
 * Get conflict definition by type.
 */
export const selectConflict = (type: string) => (state: ContentState) =>
  state.conflicts[type];

/**
 * Get quest definition by id.
 */
export const selectQuest = (id: string) => (state: ContentState) =>
  state.quests[id];

/**
 * Get event definition by id.
 */
export const selectEvent = (id: string) => (state: ContentState) =>
  state.events[id];
