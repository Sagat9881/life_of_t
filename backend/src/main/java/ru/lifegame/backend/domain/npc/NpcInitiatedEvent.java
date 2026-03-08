package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.event.game.EventOption;
import ru.lifegame.backend.domain.event.game.GameEvent;
import ru.lifegame.backend.domain.event.game.GameEventType;

import java.util.List;
import java.util.Map;

/**
 * Converts NPC behavior engine output into a standard GameEvent.
 */
public class NpcInitiatedEvent {

    public static GameEvent create(NpcActionCandidate candidate, NpcProfile npc, int day) {
        return new GameEvent(
            "npc_" + npc.code().name().toLowerCase() + "_" + candidate.id(),
            candidate.eventType(),
            npc.displayName() + ": " + candidate.title(),
            candidate.description(),
            candidate.playerOptions(),
            Map.of("npc", npc.code().name(), "npcMood", npc.mood().dominantEmotion()),
            2, // NPC events have medium priority
            day
        );
    }
}