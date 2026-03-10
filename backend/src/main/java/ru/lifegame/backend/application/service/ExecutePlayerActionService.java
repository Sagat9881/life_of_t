package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ExecuteActionCommand;
import ru.lifegame.backend.application.port.in.ExecutePlayerActionUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.EventOptionView;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.event.domain.DomainEvent;
import ru.lifegame.backend.domain.event.domain.NarrativeEventTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.QuestStepCompletedEvent;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.*;

public class ExecutePlayerActionService implements ExecutePlayerActionUseCase {

    private final SessionRepository sessionRepository;
    private final Collection<GameAction> allActions;
    private final GameStateViewMapper mapper;
    private final NarrativeEventEngine narrativeEventEngine;
    private final NarrativeQuestEngine narrativeQuestEngine;
    private final NpcLifecycleEngine npcLifecycleEngine;

    public ExecutePlayerActionService(SessionRepository sessionRepository,
                                      Collection<GameAction> allActions,
                                      GameStateViewMapper mapper,
                                      NarrativeEventEngine narrativeEventEngine,
                                      NarrativeQuestEngine narrativeQuestEngine,
                                      NpcLifecycleEngine npcLifecycleEngine) {
        this.sessionRepository = sessionRepository;
        this.allActions = allActions;
        this.mapper = mapper;
        this.narrativeEventEngine = narrativeEventEngine;
        this.narrativeQuestEngine = narrativeQuestEngine;
        this.npcLifecycleEngine = npcLifecycleEngine;
    }

    @Override
    public GameStateView execute(ExecuteActionCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));

        GameAction action = allActions.stream()
                .filter(a -> a.type().code().equals(command.actionCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown action: " + command.actionCode()));

        ActionResult result = session.executeAction(action);

        // Build String-valued context snapshot for NarrativeEventEngine
        Map<String, String> narrativeCtx = buildNarrativeContext(session, command.actionCode());

        // Build Object-valued context for NarrativeQuestEngine objective checks
        Map<String, Object> questCtx = buildQuestContext(session, command.actionCode());

        // Evaluate narrative events (data-driven from XML specs)
        if (narrativeEventEngine != null) {
            List<ru.lifegame.backend.domain.npc.spec.EventSpec> firedEvents =
                    narrativeEventEngine.evaluate(narrativeCtx);
            for (var fired : firedEvents) {
                var spec = fired;
                List<EventOptionView> options = spec.options().stream()
                        .map(o -> new EventOptionView(o.id(), o.labelRu()))
                        .toList();
                session.publishDomainEvent(new NarrativeEventTriggeredEvent(
                        session.sessionId(),
                        spec.id(),
                        spec.meta().titleRu(),
                        spec.meta().descriptionRu(),
                        options
                ));
            }
        }

        // Check quest step completion (data-driven from XML specs)
        if (narrativeQuestEngine != null) {
            for (String questId : narrativeQuestEngine.getActiveQuests().keySet()) {
                narrativeQuestEngine.tryCompleteStep(questId, questCtx).ifPresent(stepResult ->
                        session.publishDomainEvent(new QuestStepCompletedEvent(
                                session.sessionId(),
                                stepResult.questId(),
                                stepResult.stepId(),
                                stepResult.questCompleted()
                        ))
                );
            }
        }

        // Tick NPC lifecycle engine — produces activity/mood domain events
        if (npcLifecycleEngine != null) {
            Map<String, Object> npcCtx = Map.of(
                    "hour", session.time().hour(),
                    "day",  session.time().day()
            );
            List<DomainEvent> npcEvents = npcLifecycleEngine.hourlyTick(
                    session.sessionId(), session.time().hour(), npcCtx
            );
            npcEvents.forEach(session::publishDomainEvent);
        }

        sessionRepository.save(session);
        return mapper.toView(session, result);
    }

    /**
     * Builds a String-valued context map for NarrativeEventEngine.
     * All numeric stats are converted to String so condition evaluation
     * (stat_min, time_of_day, etc.) can parse them uniformly.
     */
    private Map<String, String> buildNarrativeContext(GameSession session, String actionCode) {
        Map<String, String> ctx = new LinkedHashMap<>();
        ctx.put("actionCode",  actionCode);
        ctx.put("day",         String.valueOf(session.time().day()));
        ctx.put("hour",        String.valueOf(session.time().hour()));
        ctx.put("energy",      String.valueOf(session.player().stats().energy()));
        ctx.put("health",      String.valueOf(session.player().stats().health()));
        ctx.put("stress",      String.valueOf(session.player().stats().stress()));
        ctx.put("mood",        String.valueOf(session.player().stats().mood()));
        ctx.put("money",       String.valueOf(session.player().stats().money()));
        ctx.put("selfEsteem",  String.valueOf(session.player().stats().selfEsteem()));
        session.relationships().all().forEach((npcId, rel) -> {
            ctx.put("rel." + npcId + ".closeness", String.valueOf(rel.closeness()));
            ctx.put("rel." + npcId + ".trust",     String.valueOf(rel.trust()));
            ctx.put("rel." + npcId + ".romance",   String.valueOf(rel.romance()));
        });
        return ctx;
    }

    /**
     * Builds an Object-valued context map for NarrativeQuestEngine objective checks.
     * Kept separate so quest objectives can compare numeric values without re-parsing.
     */
    private Map<String, Object> buildQuestContext(GameSession session, String actionCode) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("actionCode",  actionCode);
        ctx.put("day",         session.time().day());
        ctx.put("hour",        session.time().hour());
        ctx.put("energy",      session.player().stats().energy());
        ctx.put("health",      session.player().stats().health());
        ctx.put("stress",      session.player().stats().stress());
        ctx.put("mood",        session.player().stats().mood());
        ctx.put("money",       session.player().stats().money());
        ctx.put("selfEsteem",  session.player().stats().selfEsteem());
        session.relationships().all().forEach((npcId, rel) -> {
            ctx.put("rel." + npcId + ".closeness", rel.closeness());
            ctx.put("rel." + npcId + ".trust",     rel.trust());
            ctx.put("rel." + npcId + ".romance",   rel.romance());
        });
        return ctx;
    }
}
