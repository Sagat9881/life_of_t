package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.EndDayCommand;
import ru.lifegame.backend.application.port.in.EndDayUseCase;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.EventOptionView;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.event.domain.NarrativeEventTriggeredEvent;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.DayEndProcessor;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EndDayService implements EndDayUseCase {

    private final SessionRepository sessionRepository;
    private final GameStateViewMapper mapper;
    private final NarrativeEventEngine narrativeEventEngine;
    private final DayEndProcessor dayEndProcessor;

    public EndDayService(SessionRepository sessionRepository,
                         GameStateViewMapper mapper,
                         NarrativeEventEngine narrativeEventEngine,
                         DayEndProcessor dayEndProcessor) {
        this.sessionRepository = sessionRepository;
        this.mapper = mapper;
        this.narrativeEventEngine = narrativeEventEngine;
        this.dayEndProcessor = dayEndProcessor;
    }

    @Override
    public GameStateView endDay(EndDayCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));

        session.endDay(dayEndProcessor);

        // Evaluate narrative events at end of day
        if (narrativeEventEngine != null) {
            Map<String, String> ctx = buildEndDayContext(session);
            var firedEvents = narrativeEventEngine.evaluate(ctx);
            for (var fired : firedEvents) {
                List<EventOptionView> options = fired.options().stream()
                        .map(o -> new EventOptionView(o.id(), o.labelRu()))
                        .toList();
                session.publishDomainEvent(new NarrativeEventTriggeredEvent(
                        session.sessionId(),
                        fired.id(),
                        fired.meta().titleRu(),
                        fired.meta().descriptionRu(),
                        options
                ));
            }
        }

        sessionRepository.save(session);
        return mapper.toView(session);
    }

    /**
     * Builds a String-valued context map for NarrativeEventEngine.
     * All numeric stats are converted to String for uniform condition parsing.
     */
    private Map<String, String> buildEndDayContext(GameSession session) {
        Map<String, String> ctx = new LinkedHashMap<>();
        ctx.put("trigger",    "END_DAY");
        ctx.put("day",        String.valueOf(session.time().day()));
        ctx.put("energy",     String.valueOf(session.player().stats().energy()));
        ctx.put("health",     String.valueOf(session.player().stats().health()));
        ctx.put("stress",     String.valueOf(session.player().stats().stress()));
        ctx.put("mood",       String.valueOf(session.player().stats().mood()));
        ctx.put("money",      String.valueOf(session.player().stats().money()));
        ctx.put("selfEsteem", String.valueOf(session.player().stats().selfEsteem()));
        session.relationships().all().forEach((npcId, rel) -> {
            ctx.put("rel." + npcId + ".closeness", String.valueOf(rel.closeness()));
            ctx.put("rel." + npcId + ".trust",     String.valueOf(rel.trust()));
            ctx.put("rel." + npcId + ".romance",   String.valueOf(rel.romance()));
        });
        return ctx;
    }
}
