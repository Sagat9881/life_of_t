package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.triggers.ConflictTriggers;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.event.domain.ConflictTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.DayEndedEvent;
import ru.lifegame.backend.domain.event.domain.EndingAchievedEvent;
import ru.lifegame.backend.domain.event.domain.GameOverEvent;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;

import java.util.List;
import java.util.Map;

/**
 * Domain service responsible for end-of-day processing.
 * Handles daily decay, NPC behavior, conflict triggers, game over checks, and ending evaluation.
 */
public class DayEndProcessor {
    private final ConflictTriggers conflictTriggers;
    private final GameOverChecker gameOverChecker;
    private final EndingEvaluator endingEvaluator;
    private final NpcLifecycleEngine npcLifecycleEngine;

    public DayEndProcessor() {
        this.conflictTriggers = new ConflictTriggers();
        this.gameOverChecker = new GameOverChecker();
        this.endingEvaluator = new EndingEvaluator();
        this.npcLifecycleEngine = null;
    }

    public DayEndProcessor(
            ConflictTriggers conflictTriggers,
            GameOverChecker gameOverChecker,
            EndingEvaluator endingEvaluator,
            NpcLifecycleEngine npcLifecycleEngine
    ) {
        this.conflictTriggers = conflictTriggers;
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
            npcLifecycleEngine.dailyTick(npcContext);
        }
    }

    private void triggerNewConflicts(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        List<Conflict> newConflicts = conflictTriggers.checkTriggers(
            context.player(),
            context.relationships(),
            context.time()
        );

        List<Conflict> activeConflicts = context.activeConflicts();

        for (Conflict newConflict : newConflicts) {
            boolean alreadyExists = activeConflicts.stream()
                .anyMatch(existing ->
                    existing.type().code().equals(newConflict.type().code())
                    && !existing.isResolved()
                );

            if (!alreadyExists) {
                activeConflicts.add(newConflict);
                eventPublisher.publish(
                    new ConflictTriggeredEvent(context.sessionId(), newConflict.id())
                );
            }
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
