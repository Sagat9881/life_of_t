package com.sagat9881.lifeoft.domain.npc;

import com.sagat9881.lifeoft.domain.npc.spec.OptionSpec;
import com.sagat9881.lifeoft.domain.npc.spec.ScoredAction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapts a ScoredAction (from NPC Utility AI) into a game event.
 * Completely data-driven — no hardcoded event types or NPC names.
 * All content comes from XML spec via ScoredAction and OptionSpec.
 */
public class NpcInitiatedEvent {

    private final String npcId;
    private final String npcDisplayName;
    private final String actionId;
    private final String eventType;
    private final List<EventOption> options;
    private final double score;

    public NpcInitiatedEvent(NpcInstance npc, ScoredAction action, double finalScore) {
        this.npcId = npc.id();
        this.npcDisplayName = npc.spec().displayName();
        this.actionId = action.actionId();
        this.eventType = action.eventType();
        this.score = finalScore;
        this.options = action.options().stream()
                .map(EventOption::fromSpec)
                .collect(Collectors.toList());
    }

    public String npcId() { return npcId; }
    public String npcDisplayName() { return npcDisplayName; }
    public String actionId() { return actionId; }
    public String eventType() { return eventType; }
    public List<EventOption> options() { return options; }
    public double score() { return score; }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }

    /**
     * Player-facing option within an NPC-initiated event.
     * Built from OptionSpec — all effects are data-driven.
     */
    public record EventOption(
            String optionId,
            String text,
            String resultText,
            Map<String, Integer> statChanges,
            String relationshipTarget,
            int relationshipDelta,
            int romanceDelta,
            int trustDelta,
            Map<String, Integer> skillChanges
    ) {
        public static EventOption fromSpec(OptionSpec spec) {
            return new EventOption(
                    spec.optionId(),
                    spec.text(),
                    spec.resultText(),
                    spec.statChanges(),
                    spec.relationshipTarget(),
                    spec.relationshipDelta(),
                    spec.romanceDelta(),
                    spec.trustDelta(),
                    spec.skillChanges()
            );
        }

        public boolean affectsRelationship() {
            return relationshipTarget != null && !relationshipTarget.isBlank();
        }
    }
}
