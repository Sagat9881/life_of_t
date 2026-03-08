package com.sagat9881.lifeoft.domain.npc;

import java.util.List;

/**
 * An NPC-initiated action defined in XML with scoring data.
 * The Utility AI evaluates these to decide what the NPC does.
 *
 * @param actionId    unique action identifier from XML
 * @param baseScore   base desirability score (0.0 - 1.0)
 * @param eventType   type of game event to generate if triggered
 * @param conditions  list of conditions that must all be met
 * @param options     player choice options when event fires
 */
public record ScoredAction(
        String actionId,
        double baseScore,
        String eventType,
        List<ConditionSpec> conditions,
        List<NpcSpecLoader.ActionOption> options
) {}
