package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.ExecuteActionCommand;
import ru.lifegame.backend.application.port.in.ExecutePlayerActionUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.event.domain.NarrativeEventTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.QuestStepCompletedEvent;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.*;
import java.util.stream.Collectors;

public class ExecutePlayerActionService implements ExecutePlayerActionUseCase {

    private final SessionRepository sessionRepository;
    private final Collection<GameAction> allActions;
    private final GameStateViewMapper mapper;
    private final NarrativeEventEngine narrativeEventEngine;
    private final NarrativeQuestEngine narrativeQuestEngine;

    public ExecutePlayerActionService(SessionRepository sessionRepository,
                                      Collection<GameAction> allActions,
                                      GameStateViewMapper mapper,
                                      NarrativeEventEngine narrativeEventEngine,
                                      NarrativeQuestEngine narrativeQuestEngine) {
        this.sessionRepository = sessionRepository;
        this.allActions = allActions;
        this.mapper = mapper;
        this.narrativeEventEngine = narrativeEventEngine;
        this.narrativeQuestEngine = narrativeQuestEngine;
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

        // Build context snapshot for narrative engines
        Map<String, Object> ctx = buildNarrativeContext(session, command.actionCode());

        // Evaluate narrative events (data-driven from XML)
        if (narrativeEventEngine != null) {
            var firedEvents = narrativeEventEngine.evaluate(ctx);
            for (var fired : firedEvents) {
                var spec = fired.spec();
                var options = spec.options().stream()
                        .map(o -> Map.of("optionId", o.optionId(), "text", o.text()))
                        .collect(Collectors.toList());
                session.publishDomainEvent(new NarrativeEventTriggeredEvent(
                        session.sessionId(),
                        spec.id(),
                        spec.meta().title(),
                        spec.meta().description(),
                        options
                ));
            }
        }

        // Check quest progress (data-driven from XML)
        if (narrativeQuestEngine != null) {
            for (String questId : narrativeQuestEngine.getActiveQuests().keySet()) {
                narrativeQuestEngine.tryCompleteStep(questId, ctx).ifPresent(stepResult -> {
                    session.publishDomainEvent(new QuestStepCompletedEvent(
                            session.sessionId(),
                            stepResult.questId(),
                            stepResult.stepId(),
                            stepResult.questCompleted()
                    ));
                });
            }
        }

        sessionRepository.save(session);

        return mapper.toView(session, result);
    }

    private Map<String, Object> buildNarrativeContext(GameSession session, String actionCode) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("actionCode", actionCode);
        ctx.put("day", session.time().day());
        ctx.put("hour", session.time().hour());
        ctx.put("energy", session.player().stats().energy());
        ctx.put("health", session.player().stats().health());
        ctx.put("stress", session.player().stats().stress());
        ctx.put("mood", session.player().stats().mood());
        ctx.put("money", session.player().stats().money());
        ctx.put("selfEsteem", session.player().stats().selfEsteem());
        session.relationships().all().forEach((npcId, rel) -> {
            ctx.put("rel." + npcId + ".closeness", rel.closeness());
            ctx.put("rel." + npcId + ".trust", rel.trust());
            ctx.put("rel." + npcId + ".romance", rel.romance());
        });
        return ctx;
    }
}
