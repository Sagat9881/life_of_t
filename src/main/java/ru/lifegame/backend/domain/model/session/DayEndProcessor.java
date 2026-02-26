package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.*;
import ru.lifegame.backend.domain.conflict.triggers.ConflictTriggers;
import ru.lifegame.backend.domain.ending.*;
import ru.lifegame.backend.domain.event.domain.*;

import java.util.List;

/**
 * Domain service responsible for end-of-day processing.
 * Handles daily decay, conflict triggers, game over checks, and ending evaluation.
 */
public class DayEndProcessor {
    private final ConflictTriggers conflictTriggers;
    private final GameOverChecker gameOverChecker;
    private final EndingEvaluator endingEvaluator;

    public DayEndProcessor() {
        this.conflictTriggers = new ConflictTriggers();
        this.gameOverChecker = new GameOverChecker();
        this.endingEvaluator = new EndingEvaluator();
    }

    /**
     * Constructor for testing with custom dependencies.
     */
    public DayEndProcessor(
            ConflictTriggers conflictTriggers,
            GameOverChecker gameOverChecker,
            EndingEvaluator endingEvaluator
    ) {
        this.conflictTriggers = conflictTriggers;
        this.gameOverChecker = gameOverChecker;
        this.endingEvaluator = endingEvaluator;
    }

    /**
     * Process end of day: apply decay, trigger conflicts, check game over, evaluate endings.
     */
    public void processEndOfDay(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        // 1. Apply daily decay to all entities
        applyDailyDecay(context);

        // 2. Check and trigger new conflicts
        triggerNewConflicts(context, eventPublisher);

        // 3. Check for game over conditions
        checkGameOver(context, eventPublisher);

        // 4. Evaluate ending if game is finished (day limit reached)
        evaluateEnding(context, eventPublisher);

        // 5. Publish day ended event and advance to next day
        eventPublisher.publish(new DayEndedEvent(context.sessionId(), context.time().day()));
        context.startNewDay();
    }

    private void applyDailyDecay(GameSessionContext context) {
        context.player().applyEndOfDayDecay();
        context.relationships().applyDailyDecay(context.time().day());
        context.pets().applyDailyDecay();
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
            // Only add if no active conflict of this type exists
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
        // Only evaluate ending if:
        // 1. Game is not already over
        // 2. Max days reached
        if (context.gameOverReason() == null 
            && context.time().day() >= GameBalance.MAX_GAME_DAYS) {
            
            // EndingEvaluator uses old class structure - temporarily comment out
            // TODO: Fix EndingEvaluator to use new package structure
            /*
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
            */
        }
    }
}
