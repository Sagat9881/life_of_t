package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.conflict.triggers.ConflictTriggers;
import ru.lifegame.backend.domain.ending.EndingEvaluator;
import ru.lifegame.backend.domain.event.domain.ConflictTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.DayEndedEvent;
import ru.lifegame.backend.domain.event.domain.EndingAchievedEvent;
import ru.lifegame.backend.domain.event.domain.EventTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.GameOverEvent;
import ru.lifegame.backend.domain.event.game.GameEvent;

import java.util.List;
import java.util.Optional;

/**
 * Domain service responsible for end-of-day processing.
 * Handles daily decay, NPC behavior, conflict triggers, game over checks, and ending evaluation.
 */
public class DayEndProcessor {
    private final ConflictTriggers conflictTriggers;
    private final GameOverChecker gameOverChecker;
    private final EndingEvaluator endingEvaluator;
    private final NpcBehaviorEngine npcBehaviorEngine;

    public DayEndProcessor() {
        this.conflictTriggers = new ConflictTriggers();
        this.gameOverChecker = new GameOverChecker();
        this.endingEvaluator = new EndingEvaluator();
        this.npcBehaviorEngine = new NpcBehaviorEngine();
    }

    /**
     * Constructor for testing with custom dependencies.
     */
    public DayEndProcessor(
            ConflictTriggers conflictTriggers,
            GameOverChecker gameOverChecker,
            EndingEvaluator endingEvaluator,
            NpcBehaviorEngine npcBehaviorEngine
    ) {
        this.conflictTriggers = conflictTriggers;
        this.gameOverChecker = gameOverChecker;
        this.endingEvaluator = endingEvaluator;
        this.npcBehaviorEngine = npcBehaviorEngine;
    }

    /**
     * Process end of day: apply decay, NPC behavior, trigger conflicts, check game over, evaluate endings.
     */
    public void processEndOfDay(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        // 1. Apply daily decay to all entities
        applyDailyDecay(context);

        // 2. Tick NPC moods and run behavior engine
        processNpcBehavior(context, eventPublisher);

        // 3. Check and trigger new conflicts
        triggerNewConflicts(context, eventPublisher);

        // 4. Check for game over conditions
        checkGameOver(context, eventPublisher);

        // 5. Evaluate ending if game is finished (day limit reached)
        evaluateEnding(context, eventPublisher);

        // 6. Publish day ended event and advance to next day
        eventPublisher.publish(new DayEndedEvent(context.sessionId(), context.time().day()));

        // 7. Reset NPC daily state and advance day
        npcBehaviorEngine.resetDaily();
        context.startNewDay();
    }

    private void applyDailyDecay(GameSessionContext context) {
        context.player().applyEndOfDayDecay();
        context.relationships().applyDailyDecay(context.time().day());
        context.pets().applyDailyDecay();
    }

    /**
     * Phase 6: NPC behavior processing.
     * 1. Update NPC moods based on relationship state.
     * 2. Evaluate each NPC's behavior candidates via Utility AI.
     * 3. If an NPC wants to initiate, create a GameEvent.
     */
    private void processNpcBehavior(
            GameSessionContext context,
            DomainEventPublisher eventPublisher
    ) {
        context.npcProfiles().dailyTick(context.relationships(), context.time().day());

        int hour = context.time().hour();
        int day = context.time().day();

        context.npcProfiles().all().forEach((code, profile) -> {
            Optional<GameEvent> event = npcBehaviorEngine.evaluate(profile, day, hour);
            event.ifPresent(e -> {
                e.markTriggered();
                context.events().add(e);
                eventPublisher.publish(new EventTriggeredEvent(context.sessionId(), e.id()));
            });
        });
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