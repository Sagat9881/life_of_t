package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.engine.ConflictEngine;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.event.domain.DayEndedEvent;
import ru.lifegame.backend.domain.event.domain.DomainEvent;
import ru.lifegame.backend.domain.event.domain.EndingAchievedEvent;
import ru.lifegame.backend.domain.event.domain.GameOverEvent;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain service responsible for end-of-day processing.
 * Updated to use ConflictEngine (data-driven) instead of ConflictTriggers (hardcoded).
 */
public class DayEndProcessor {
    private final ConflictEngine conflictEngine;
    private final ConflictManager conflictManager;
    private final GameOverChecker gameOverChecker;
    private final EndingEvaluator endingEvaluator;
    private final NpcLifecycleEngine npcLifecycleEngine;

    public DayEndProcessor(
            ConflictEngine conflictEngine,
            ConflictManager conflictManager,
            GameOverChecker gameOverChecker,
            EndingEvaluator endingEvaluator,
            NpcLifecycleEngine npcLifecycleEngine
    ) {
        this.conflictEngine = conflictEngine;
        this.conflictManager = conflictManager;
        this.gameOverChecker = gameOverChecker;
        this.endingEvaluator = endingEvaluator;
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
        // Add any extra context (e.g., quest state, story flags)
        
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

    private void checkGameOver(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        gameOverChecker.check(
            context.player(),
            context.relationships(),
            context.pets()
        ).ifPresent(reason -> {
            context.setGameOverReason(reason);
            eventPublisher.publish(
                new GameOverEvent(context.sessionId(), reason.name())
            );
        });
    }

    private void evaluateEnding(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        if (context.gameOverReason() == null
            && context.time().day() >= GameBalance.MAX_GAME_DAYS) {

            endingEvaluator.findBestEnding(
                context.player(),
                context.relationships(),
                context.pets(),
                context.questLog(),
                context.time()
            ).ifPresent(ending -> {
                context.setEnding(ending);
                eventPublisher.publish(
                    new EndingAchievedEvent(context.sessionId(), ending.type().name())
                );
            });
        }
    }
}
