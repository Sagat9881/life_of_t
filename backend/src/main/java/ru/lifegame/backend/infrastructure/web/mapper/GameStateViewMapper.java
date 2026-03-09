package ru.lifegame.backend.infrastructure.web.mapper;

import ru.lifegame.backend.application.view.*;
import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.event.domain.DomainEvent;
import ru.lifegame.backend.domain.event.domain.NarrativeEventTriggeredEvent;
import ru.lifegame.backend.domain.event.domain.QuestStepCompletedEvent;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pet;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.Relationship;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.model.session.GameTime;
import ru.lifegame.backend.domain.model.stats.Stats;
import ru.lifegame.backend.domain.npc.runtime.NpcInstance;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;
import ru.lifegame.backend.domain.quest.Quest;

import java.util.*;

public class GameStateViewMapper {

    private final List<GameAction> allActions;
    private final NpcLifecycleEngine npcLifecycleEngine;

    public GameStateViewMapper(List<GameAction> allActions, NpcLifecycleEngine npcLifecycleEngine) {
        this.allActions = allActions;
        this.npcLifecycleEngine = npcLifecycleEngine;
    }

    public GameStateView toView(GameSession session, ActionResult lastResult) {
        List<DomainEvent> events = session.drainDomainEvents();
        return new GameStateView(
                session.sessionId(),
                String.valueOf(session.telegramUserId()),
                toPlayerView(session.player()),
                toRelationshipViews(session.relationships()),
                toPetViews(session.pets()),
                toTimeView(session.time()),
                toActionOptionViews(session),
                toQuestViews(session),
                toCompletedQuestIds(session),
                toConflictViews(session),
                toEventView(session),
                toEndingView(session),
                lastResult != null ? toActionResultView(lastResult) : null,
                toNpcActivityViews(session),
                toDomainEventViews(events)
        );
    }

    public GameStateView toView(GameSession session) {
        return toView(session, null);
    }

    private TimeView toTimeView(GameTime time) {
        return new TimeView(time.day(), time.hour(), resolveTimeSlot(time.hour()), time.isDayOver());
    }

    static String resolveTimeSlot(int hour) {
        if (hour >= 24) return "NIGHT";
        if (hour < 7) return "NIGHT";
        if (hour < 12) return "MORNING";
        if (hour < 17) return "DAY";
        if (hour < 21) return "EVENING";
        return "NIGHT";
    }

    private PlayerView toPlayerView(PlayerCharacter p) {
        Stats s = p.stats();
        return new PlayerView(
                p.id().value(),
                p.name(),
                new StatsView(s.energy(), s.health(), s.stress(), s.mood(), s.money(), s.selfEsteem()),
                new JobView(p.job().title(), p.job().satisfaction(), p.job().burnoutRisk()),
                p.location().name(),
                p.tags(),
                p.skills().toMap(),
                p.inventory()
        );
    }

    private List<RelationshipView> toRelationshipViews(Relationships rels) {
        return rels.all().entrySet().stream()
                .map(e -> {
                    Relationship r = e.getValue();
                    String npcId = e.getKey();
                    return new RelationshipView(
                            npcId, npcId,
                            r.closeness(), r.trust(), r.stability(), r.romance()
                    );
                })
                .toList();
    }

    private List<PetView> toPetViews(Pets pets) {
        return pets.all().entrySet().stream()
                .map(e -> {
                    Pet p = e.getValue();
                    String petId = e.getKey();
                    return new PetView(petId, petId,
                            p.name(), p.satiety(), p.attention(), p.health(), p.mood());
                })
                .toList();
    }

    private List<ActionOptionView> toActionOptionViews(GameSession session) {
        return allActions.stream()
                .map(action -> {
                    int timeCost = action.calculateTimeCost(session.context().asReadModel());
                    boolean available = !session.time().isDayOver()
                            && session.player().canPerformAction(action.type(), session.time(), timeCost);
                    String reason = available ? null : resolveUnavailableReason(session, timeCost);
                    return new ActionOptionView(
                            action.type().code(), action.type().label(),
                            action.type().description(), timeCost, available, reason
                    );
                })
                .toList();
    }

    private String resolveUnavailableReason(GameSession session, int timeCost) {
        if (session.time().isDayOver()) return "День закончен";
        if (!session.time().hasEnoughTime(timeCost)) return "Недостаточно времени";
        if (session.player().stats().energy() < 5) return "Недостаточно энергии";
        return "Действие недоступно";
    }

    private List<QuestView> toQuestViews(GameSession session) {
        return session.questLog().activeQuests().stream()
                .map(q -> new QuestView(q.id(), q.type().label(),
                        q.type().description(), q.progressPercent(), q.isCompleted()))
                .toList();
    }

    private List<String> toCompletedQuestIds(GameSession session) {
        return session.questLog().completedQuests().stream()
                .map(Quest::id)
                .toList();
    }

    private List<ConflictView> toConflictViews(GameSession session) {
        return session.activeConflicts().stream()
                .filter(c -> !c.isResolved())
                .map(c -> new ConflictView(
                        c.id(), c.type().code(), c.type().label(),
                        c.stage().name(), c.csp().player(), c.csp().opponent(),
                        toTacticOptions(session.player(), c)
                ))
                .toList();
    }

    private List<TacticOptionView> toTacticOptions(PlayerCharacter player, Conflict conflict) {
        return player.availableConflictTactics().stream()
                .map(t -> new TacticOptionView(t.code(), t.label(), t.description()))
                .toList();
    }

    private EventView toEventView(GameSession session) {
        return session.events().stream()
                .filter(e -> e.isTriggered() && !e.isResolved())
                .findFirst()
                .map(e -> new EventView(e.id(), e.type().label(),
                        e.type().description(),
                        e.options().stream()
                                .map(o -> new EventOptionView(o.code(), o.label(), o.description()))
                                .toList()))
                .orElse(null);
    }

    private EndingView toEndingView(GameSession session) {
        if (session.ending() == null) return null;
        return new EndingView(
                session.ending().type().name(),
                session.ending().category().name(),
                session.ending().summary()
        );
    }

    private ActionResultView toActionResultView(ActionResult r) {
        return new ActionResultView(
                r.actionType().code(), r.timeCost(), r.description(),
                Map.of("energy", r.statChanges().energy(), "health", r.statChanges().health(),
                        "stress", r.statChanges().stress(), "mood", r.statChanges().mood(),
                        "money", r.statChanges().money(), "selfEsteem", r.statChanges().selfEsteem()),
                r.relationshipChanges(),
                r.petMoodChanges()
        );
    }

    private List<NpcActivityView> toNpcActivityViews(GameSession session) {
        if (npcLifecycleEngine == null) return List.of();
        int currentHour = session.time().hour();
        // Clamp hour for NPC schedule lookup (24 is end-of-day sentinel)
        int lookupHour = Math.min(currentHour, 23);
        return npcLifecycleEngine.getRegistry().getAll().stream()
                .map(npc -> {
                    npc.updateScheduleActivity(lookupHour);
                    var activity = npc.currentActivity();
                    return NpcActivityView.fromInstance(
                            npc.id(),
                            npc.displayName(),
                            npc.category(),
                            activity.activityId(),
                            activity.animationKey(),
                            activity.locationId(),
                            npc.mood().dominantAxis(),
                            true
                    );
                })
                .toList();
    }

    private List<DomainEventView> toDomainEventViews(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) return List.of();
        return events.stream()
                .map(this::mapDomainEvent)
                .toList();
    }

    private DomainEventView mapDomainEvent(DomainEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", event.sessionId());

        if (event instanceof NarrativeEventTriggeredEvent ne) {
            payload.put("narrativeEventId", ne.narrativeEventId());
            payload.put("title", ne.title());
            payload.put("description", ne.description());
            payload.put("options", ne.options());
        } else if (event instanceof QuestStepCompletedEvent qe) {
            payload.put("questId", qe.questId());
            payload.put("stepId", qe.stepId());
            payload.put("questCompleted", qe.questCompleted());
        }

        return new DomainEventView(
                event.eventType(),
                event.timestamp().toString(),
                payload
        );
    }
}
