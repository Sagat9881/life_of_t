/**
 * Data-driven NPC behavior engine.
 * 
 * Architecture:
 * - XML specs (narrative/npc-behavior/*.xml) → NpcSpecLoader → NpcSpec records
 * - NpcRegistry creates NpcInstance (named or filler) from specs
 * - NpcUtilityBrain evaluates ScoredActions via ConditionEvaluator → picks best activity
 * - NpcLifecycleEngine runs hourlyTick (activity) and dailyTick (mood decay, memory cleanup)
 * - CrossNpcTriggerEngine checks inter-NPC dynamics (jealousy, tension)
 * - NpcInitiatedEvent bridges NPC decisions → GameEvent system
 *
 * Key principle: backend is a generic engine. All NPC names, actions, schedules,
 * personalities, and behaviors come from XML specifications.
 */
package com.sagat9881.lifeoft.domain.npc.engine;
