/**
 * NPC runtime model — mutable state objects for live NPC instances.
 * 
 * - NpcInstance: live NPC = spec + mood + memory + schedule + current activity
 * - NpcMood: 6 independent axes (happiness, anxiety, loneliness, irritability, energy, affection)
 * - NpcMemory: short-term + long-term event memory with pattern detection
 * - NpcSchedule: daily time slots from XML spec
 * - NpcActivity: current physical action (what animation to play, where)
 */
package com.sagat9881.lifeoft.domain.npc.model;
