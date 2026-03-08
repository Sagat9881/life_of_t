package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.model.session.GameSessionContext;
import com.sagat9881.lifeoft.domain.event.game.GameEvent;
import com.sagat9881.lifeoft.domain.event.game.GameEventType;
import com.sagat9881.lifeoft.domain.event.game.EventOption;
import com.sagat9881.lifeoft.domain.model.character.StatChanges;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks cross-NPC interaction conditions and generates events.
 * E.g., if player spends too much time with one NPC, another gets jealous.
 * If tension between two NPCs exceeds threshold, they fight — player picks a side.
 * All thresholds and triggers are data-driven via NpcRelationshipGraph.
 */
public class CrossNpcTriggerEngine {

    private final NpcRelationshipGraph graph;
    private final NpcRegistry registry;

    public CrossNpcTriggerEngine(NpcRelationshipGraph graph, NpcRegistry registry) {
        this.graph = graph;
        this.registry = registry;
    }

    /**
     * Check for cross-NPC events. Called during endDay processing.
     */
    public List<GameEvent> checkTriggers(GameSessionContext context) {
        List<GameEvent> events = new ArrayList<>();

        // Check high-tension NPC pairs
        List<NpcRelationshipGraph.NpcNpcRelation> tensePairs = graph.highTensionPairs(70);
        for (var pair : tensePairs) {
            var npcA = registry.get(pair.npcA());
            var npcB = registry.get(pair.npcB());
            if (npcA.isPresent() && npcB.isPresent()) {
                events.add(createConflictEvent(npcA.get(), npcB.get(), pair));
            }
        }

        // Check jealousy: if named NPC has high loneliness + player interacts often with another
        for (NpcInstance npc : registry.named()) {
            if (npc.mood().loneliness() > 60 && npc.memory() != null && npc.memory().isBeingIgnored(5)) {
                // Check if another NPC is getting attention
                for (NpcInstance other : registry.named()) {
                    if (!other.spec().id().equals(npc.spec().id())
                            && other.memory() != null
                            && other.memory().hasRecentInteraction("any", 2)) {
                        events.add(createJealousyEvent(npc, other));
                        break; // One jealousy event per NPC per day
                    }
                }
            }
        }

        return events;
    }

    /**
     * Update tension between NPCs based on player actions.
     * Called when player interacts with an NPC.
     */
    public void onPlayerInteraction(String interactedNpcId, GameSessionContext context) {
        // Increase tension slightly for neglected NPCs
        for (NpcInstance npc : registry.named()) {
            if (!npc.spec().id().equals(interactedNpcId)) {
                graph.updateTension(npc.spec().id(), interactedNpcId, 2);
            }
        }
        // Decrease tension with interacted NPC
        for (NpcInstance npc : registry.named()) {
            if (!npc.spec().id().equals(interactedNpcId)) {
                graph.updateTension(interactedNpcId, npc.spec().id(), -1);
            }
        }
    }

    private GameEvent createConflictEvent(NpcInstance npcA, NpcInstance npcB,
                                          NpcRelationshipGraph.NpcNpcRelation relation) {
        String eventId = "cross_conflict_" + npcA.spec().id() + "_" + npcB.spec().id();
        String title = npcA.spec().displayName() + " и " + npcB.spec().displayName() + " конфликтуют";
        String description = "Напряжение между " + npcA.spec().displayName()
                + " и " + npcB.spec().displayName() + " достигло предела.";

        List<EventOption> options = List.of(
                new EventOption("side_a", "Поддержать " + npcA.spec().displayName(),
                        npcA.spec().displayName() + " благодарен за поддержку",
                        new StatChanges(0, 10, -5, 0)),
                new EventOption("side_b", "Поддержать " + npcB.spec().displayName(),
                        npcB.spec().displayName() + " благодарен за поддержку",
                        new StatChanges(0, 10, -5, 0)),
                new EventOption("mediate", "Попытаться примирить",
                        "Ты попытался найти компромисс",
                        new StatChanges(-10, 15, 0, 0))
        );

        return new GameEvent(eventId, title, description, GameEventType.RELATIONSHIP_MILESTONE, options);
    }

    private GameEvent createJealousyEvent(NpcInstance jealousNpc, NpcInstance rivalNpc) {
        String eventId = "jealousy_" + jealousNpc.spec().id() + "_" + rivalNpc.spec().id();
        String title = jealousNpc.spec().displayName() + " ревнует";
        String description = jealousNpc.spec().displayName() + " заметил, что ты проводишь много времени с "
                + rivalNpc.spec().displayName() + ".";

        List<EventOption> options = List.of(
                new EventOption("reassure", "Объяснить и успокоить",
                        jealousNpc.spec().displayName() + " немного успокоился",
                        new StatChanges(-5, 5, 5, 0)),
                new EventOption("dismiss", "Отмахнуться",
                        jealousNpc.spec().displayName() + " обиделся ещё больше",
                        new StatChanges(0, -5, -10, 0))
        );

        return new GameEvent(eventId, title, description, GameEventType.RANDOM_ENCOUNTER, options);
    }
}
