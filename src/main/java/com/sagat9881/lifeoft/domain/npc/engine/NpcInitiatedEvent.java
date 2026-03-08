package com.sagat9881.lifeoft.domain.npc.engine;

import com.sagat9881.lifeoft.domain.npc.model.NpcInstance;
import com.sagat9881.lifeoft.domain.npc.spec.ScoredAction;
import com.sagat9881.lifeoft.domain.event.game.GameEvent;
import com.sagat9881.lifeoft.domain.event.game.GameEventType;
import com.sagat9881.lifeoft.domain.event.game.EventOption;
import com.sagat9881.lifeoft.domain.model.character.StatChanges;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts an NPC's ScoredAction into a GameEvent that the player can respond to.
 * Bridge between NPC Utility AI decisions and the existing event system.
 */
public class NpcInitiatedEvent {

    public static GameEvent from(NpcInstance npc, ScoredAction action) {
        List<EventOption> options = action.options().stream()
                .map(opt -> new EventOption(
                        opt.id(),
                        opt.text(),
                        opt.resultText(),
                        new StatChanges(
                                opt.energyDelta(),
                                opt.stressDelta(),
                                opt.moodDelta(),
                                opt.moneyDelta()
                        )
                ))
                .collect(Collectors.toList());

        GameEventType eventType = parseEventType(action.eventType());

        return new GameEvent(
                "npc_" + npc.spec().id() + "_" + action.actionId(),
                npc.spec().displayName() + ": " + action.actionId().replace("_", " "),
                action.description() != null ? action.description() : "",
                eventType,
                options
        );
    }

    private static GameEventType parseEventType(String type) {
        if (type == null) return GameEventType.RANDOM_ENCOUNTER;
        try {
            return GameEventType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return GameEventType.RANDOM_ENCOUNTER;
        }
    }
}
