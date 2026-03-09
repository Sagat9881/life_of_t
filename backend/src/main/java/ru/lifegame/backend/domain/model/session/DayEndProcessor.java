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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Domain service responsible for end-of-day processing.
 * Now fully data-driven: uses ConflictEngine and EndingEngine.
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
        Map<String, Object> additionalContext = new HashMap<>();
        
        List<Conflict> newConflicts = conflictEngine.evaluateTriggers(
            context.player(),
            context.relationships(),
            context.time(),
            additionalContext
        );

        for (Conflict newConflict : newConflicts) {
            conflictManager.addNewConflict(newConflict, context, eventPublisher);
        }
    }

    /**
     * Check for game-over conditions using EndingEngine (data-driven).
     */
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
                new GameOverEvent(context.sessionId(), ending.type().name())
            );
        });
    }

    /**
     * Evaluate story ending on day 30 using EndingEngine (data-driven).
     */
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
                    new EndingAchievedEvent(context.sessionId(), ending.type().name())
                );
            });
        }
    }
}
