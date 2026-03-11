package ru.lifegame.backend.application.service;

import ru.lifegame.backend.application.command.EndDayCommand;
import ru.lifegame.backend.application.port.in.EndDayUseCase;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.application.port.out.SessionRepository;
import ru.lifegame.backend.application.view.GameStateView;
import ru.lifegame.backend.domain.event.domain.NarrativeEventTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.QuestActivatedEvent;
import ru.lifegame.backend.domain.event.game.GameEvent;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.session.DayEndProcessor;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.narrative.EventSpecMapper;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.domain.narrative.spec.EventSpec;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EndDayService implements EndDayUseCase {

    private final SessionRepository sessionRepository;
    private final EventPublisher eventPublisher;
    private final GameStateViewMapper mapper;
    private final NarrativeEventEngine narrativeEventEngine;
    private final NarrativeQuestEngine narrativeQuestEngine;
    private final DayEndProcessor dayEndProcessor;

    public EndDayService(SessionRepository sessionRepository,
                         EventPublisher eventPublisher,
                         GameStateViewMapper mapper,
                         NarrativeEventEngine narrativeEventEngine,
                         NarrativeQuestEngine narrativeQuestEngine,
                         DayEndProcessor dayEndProcessor) {
        this.sessionRepository = sessionRepository;
        this.eventPublisher = eventPublisher;
        this.mapper = mapper;
        this.narrativeEventEngine = narrativeEventEngine;
        this.narrativeQuestEngine = narrativeQuestEngine;
        this.dayEndProcessor = dayEndProcessor;
    }

    @Override
    public GameStateView endDay(EndDayCommand command) {
        GameSession session = sessionRepository.findByTelegramUserId(command.telegramUserId())
                .orElseThrow(() -> new SessionNotFoundException(command.telegramUserId()));

        session.endDay(dayEndProcessor);

        int currentDay = session.time().day();

        // ── Auto-activate quests whose triggerDay has arrived ────────────────────
        if (narrativeQuestEngine != null) {
            narrativeQuestEngine.getQuestSpecs().stream()
                    .filter(q -> q.meta().triggerDay() <= currentDay)
                    .filter(q -> !narrativeQuestEngine.getActiveQuests().containsKey(q.id()))
                    .forEach(q -> {
                        narrativeQuestEngine.activateQuest(q.id());
                        session.publishDomainEvent(new QuestActivatedEvent(
                                session.sessionId(),
                                q.id(),
                                q.meta().title()
                        ));
                    });
        }

        // ── Narrative events at end of day ────────────────────────────────
        if (narrativeEventEngine != null) {
            Map<String, String> ctx = buildEndDayContext(session);
            List<EventSpec> firedSpecs = narrativeEventEngine.evaluate(ctx);
            for (EventSpec spec : firedSpecs) {
                // 1. Convert spec -> GameEvent and register in session
                GameEvent gameEvent = EventSpecMapper.toGameEvent(spec, currentDay);
                session.triggerEvent(gameEvent);

                // 2. Notify frontend via SSE
                List<NarrativeEventTriggeredEvent.NarrativeOption> optionViews = spec.options().stream()
                        .map(o -> new NarrativeEventTriggeredEvent.NarrativeOption(o.id(), o.labelRu()))
                        .toList();
                session.publishDomainEvent(new NarrativeEventTriggeredEvent(
                        session.sessionId(),
                        spec.id(),
                        spec.meta().titleRu(),
                        spec.meta().descriptionRu(),
                        optionViews
                ));
            }
        }

        session.drainDomainEvents().forEach(eventPublisher::publish);
        sessionRepository.save(session);
        return mapper.toView(session);
    }

    /**
     * String-valued context for NarrativeEventEngine condition evaluation.
     * All numeric stats converted to String for uniform stat_min/time_of_day parsing.
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
