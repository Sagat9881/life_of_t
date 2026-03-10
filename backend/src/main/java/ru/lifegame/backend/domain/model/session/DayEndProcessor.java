package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.engine.ConflictEngine;
import ru.lifegame.backend.domain.ending.Ending;
import ru.lifegame.backend.domain.ending.EndingEngine;
import ru.lifegame.backend.domain.event.domain.DayEndedEvent;
import ru.lifegame.backend.domain.event.domain.DomainEvent;
import ru.lifegame.backend.domain.event.domain.EndingAchievedEvent;
import ru.lifegame.backend.domain.event.domain.GameOverEvent;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;
import ru.lifegame.backend.domain.quest.Quest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Domain service responsible for end-of-day processing.
 * Fully data-driven: uses ConflictEngine and EndingEngine.
 */
public class DayEndProcessor {
    private final ConflictEngine conflictEngine;
    private final ConflictManager conflictManager;
    private final EndingEngine endingEngine;
    private final NpcLifecycleEngine npcLifecycleEngine;

    public DayEndProcessor(
            ConflictEngine conflictEngine,
            ConflictManager conflictManager,
            EndingEngine endingEngine,
            NpcLifecycleEngine npcLifecycleEngine
    ) {
        this.conflictEngine = conflictEngine;
        this.conflictManager = conflictManager;
        this.endingEngine = endingEngine;
        this.npcLifecycleEngine = npcLifecycleEngine;
    }

    public void processEndOfDay(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        applyDailyDecay(context);
        processNpcBehavior(context, eventPublisher);
        triggerNewConflicts(context, eventPublisher);
        checkGameOver(context, eventPublisher);
        evaluateEnding(context, eventPublisher);
        eventPublisher.publish(new DayEndedEvent(context.sessionId(), context.time().day()));
        context.startNewDay();
    }

    private void applyDailyDecay(GameSessionContext context) {
        context.player().applyEndOfDayDecay();
        context.relationships().applyDailyDecay(context.time().day());
        context.pets().applyDailyDecay();
    }

    private void processNpcBehavior(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        if (npcLifecycleEngine != null) {
            Map<String, Object> npcContext = Map.of(
                    "hour", context.time().hour(),
                    "day", context.time().day()
            );
            List<DomainEvent> npcEvents = npcLifecycleEngine.dailyTick(
                    context.sessionId(), npcContext
            );
            npcEvents.forEach(eventPublisher::publish);
        }
    }

    private void triggerNewConflicts(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        Map<String, Object> additionalContext = buildAdditionalContext(context);

        List<Conflict> newConflicts = conflictEngine.evaluateTriggers(
                context.player(),
                context.relationships(),
                context.time(),
                additionalContext
        );

        for (Conflict newConflict : newConflicts) {
            try {
                conflictManager.addNewConflict(newConflict, context, eventPublisher);
            } catch (ru.lifegame.backend.domain.exception.InvalidGameStateException ignored) {
                // Conflict of this type already active — skip silently
            }
        }
    }

    /**
     * Build the full additional context map for ConflictEngine trigger evaluation.
     * Provides quest states, player counters, and time-spent data that are
     * referenced by trigger conditions in conflict XML specs.
     */
    private Map<String, Object> buildAdditionalContext(GameSessionContext context) {
        Map<String, Object> ctx = new HashMap<>();

        // Quest states: questId -> "ACTIVE" | "COMPLETED" | "FAILED" | "LOCKED"
        Map<String, String> questStates = new HashMap<>();
        for (Map.Entry<String, Quest> entry : context.questLog().all().entrySet()) {
            Quest q = entry.getValue();
            String state;
            if (q.isCompleted()) state = "COMPLETED";
            else if (q.isActive()) state = "ACTIVE";
            else state = "LOCKED";
            questStates.put(entry.getKey(), state);
        }
        ctx.put("questStates", questStates);

        // Player counters used by trigger conditions
        ctx.put("consecutiveWorkDays", context.player().consecutiveWorkDays());
        ctx.put("daysSinceHousehold", context.player().daysSinceHousehold());

        // Days played is simply current day number
        ctx.put("daysPlayed", context.time().day());

        return ctx;
    }

    private void checkGameOver(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        Optional<Ending> gameOverEnding = endingEngine.checkGameOver(
                context.player(),
                context.relationships(),
                context.pets(),
                context.questLog()
        );
        gameOverEnding.ifPresent(ending -> {
            context.setEnding(ending);
            eventPublisher.publish(
                    new GameOverEvent(context.sessionId(), ending.endingId())
            );
        });
    }

    private void evaluateEnding(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        if (context.ending() == null
                && context.time().day() >= GameBalance.MAX_GAME_DAYS) {
            endingEngine.findBestStoryEnding(
                    context.player(),
                    context.relationships(),
                    context.pets(),
                    context.questLog()
            ).ifPresent(ending -> {
                context.setEnding(ending);
                eventPublisher.publish(
                        new EndingAchievedEvent(context.sessionId(), ending.endingId())
                );
            });
        }
    }
}
