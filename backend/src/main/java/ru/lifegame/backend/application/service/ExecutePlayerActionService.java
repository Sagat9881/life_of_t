package ru.lifegame.backend.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ru.lifegame.backend.domain.event.game.GameEvent;
import ru.lifegame.backend.domain.exception.SessionNotFoundException;
import ru.lifegame.backend.domain.model.relationship.RelationshipChanges;
import ru.lifegame.backend.domain.model.session.GameSession;
import ru.lifegame.backend.domain.model.stats.StatChanges;
import ru.lifegame.backend.domain.narrative.EventSpecMapper;
import ru.lifegame.backend.domain.narrative.NarrativeEventEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine;
import ru.lifegame.backend.domain.narrative.NarrativeQuestEngine.StepCompletionResult;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.RewardSpec;
import ru.lifegame.backend.domain.npc.spec.EventSpec;
import ru.lifegame.backend.domain.npc.runtime.NpcLifecycleEngine;
import ru.lifegame.backend.infrastructure.web.mapper.GameStateViewMapper;

import java.util.*;

public class ExecutePlayerActionService implements ExecutePlayerActionUseCase {

    private static final Logger log = LoggerFactory.getLogger(ExecutePlayerActionService.class);

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

        Map<String, String> narrativeCtx = buildNarrativeContext(session, command.actionCode());
        Map<String, Object> questCtx     = buildQuestContext(session, command.actionCode());

        // ── Narrative events ────────────────────────────────────────────────────────────
        if (narrativeEventEngine != null) {
            List<EventSpec> firedSpecs = narrativeEventEngine.evaluate(narrativeCtx);
            for (EventSpec spec : firedSpecs) {
                GameEvent gameEvent = EventSpecMapper.toGameEvent(spec, session.time().day());
                session.triggerEvent(gameEvent);
                List<EventOptionView> optionViews = spec.options().stream()
                        .map(o -> new EventOptionView(o.id(), o.labelRu()))
                        .toList();
                session.publishDomainEvent(new NarrativeEventTriggeredEvent(
                        session.sessionId(), spec.id(),
                        spec.meta().titleRu(), spec.meta().descriptionRu(),
                        optionViews));
            }
        }

        // ── Quest steps ───────────────────────────────────────────────────────────────────
        if (narrativeQuestEngine != null) {
            for (String questId : narrativeQuestEngine.getActiveQuests().keySet()) {
                narrativeQuestEngine.tryCompleteStep(questId, questCtx)
                        .ifPresent(stepResult -> {
                            applyQuestRewards(stepResult, session);
                            session.publishDomainEvent(new QuestStepCompletedEvent(
                                    session.sessionId(),
                                    stepResult.questId(),
                                    stepResult.stepId(),
                                    stepResult.questCompleted(),
                                    stepResult.rewards()));
                        });
            }
        }

        // ── NPC lifecycle tick ───────────────────────────────────────────────────────────
        if (npcLifecycleEngine != null) {
            Map<String, Object> npcCtx = Map.of(
                    "hour", session.time().hour(),
                    "day",  session.time().day());
            List<DomainEvent> npcEvents = npcLifecycleEngine.hourlyTick(
                    session.sessionId(), session.time().hour(), npcCtx);
            npcEvents.forEach(session::publishDomainEvent);
        }

        sessionRepository.save(session);
        return mapper.toView(session, result);
    }

    // ── reward application ───────────────────────────────────────────────────────────

    /**
     * Applies quest step rewards to the session.
     *
     * Supported reward types:
     *   stat         — target = stat name, amount = delta
     *   skill        — target = skill name, amount = xp
     *   relationship — target = "NPC_ID:field" (e.g. "HUSBAND:closeness"),
     *                   amount = delta applied to that specific field
     *   achievement  — informational marker (no-op; used by frontend only)
     */
    private void applyQuestRewards(StepCompletionResult result, GameSession session) {
        if (result.rewards() == null || result.rewards().isEmpty()) return;

        int energy = 0, health = 0, stress = 0, mood = 0, money = 0, selfEsteem = 0;

        for (RewardSpec reward : result.rewards()) {
            switch (reward.type()) {
                case "stat" -> {
                    switch (normaliseStat(reward.target())) {
                        case "energy"     -> energy     += reward.amount();
                        case "health"     -> health     += reward.amount();
                        case "stress"     -> stress     += reward.amount();
                        case "mood"       -> mood       += reward.amount();
                        case "money"      -> money      += reward.amount();
                        case "selfesteem" -> selfEsteem += reward.amount();
                        default -> log.warn("[QuestRewards] Unknown stat target: '{}'", reward.target());
                    }
                }
                case "skill" ->
                    session.player().improveSkill(reward.target(), reward.amount());

                case "relationship" -> {
                    // target format: "NPC_ID:field"  e.g. "HUSBAND:closeness"
                    String[] parts = reward.target().split(":", 2);
                    if (parts.length != 2) {
                        log.warn("[QuestRewards] Invalid relationship target '{}', expected 'NPC_ID:field'",
                                reward.target());
                        break;
                    }
                    String npcId = parts[0];
                    String field = parts[1].toLowerCase();
                    int    delta = reward.amount();
                    RelationshipChanges changes = switch (field) {
                        case "closeness"  -> new RelationshipChanges(npcId, delta,  0,     0,     0);
                        case "trust"      -> new RelationshipChanges(npcId, 0,      delta, 0,     0);
                        case "stability"  -> new RelationshipChanges(npcId, 0,      0,     delta, 0);
                        case "romance"    -> new RelationshipChanges(npcId, 0,      0,     0,     delta);
                        default -> {
                            log.warn("[QuestRewards] Unknown relationship field '{}' for npc '{}'",
                                    field, npcId);
                            yield null;
                        }
                    };
                    if (changes != null) {
                        session.relationships().applyChanges(npcId, changes);
                    }
                }

                case "achievement" ->
                    // Achievements are informational markers surfaced to the frontend
                    // via QuestStepCompletedEvent.rewards[]. No server-side state needed.
                    log.info("[QuestRewards] Achievement unlocked: '{}' (quest: {})",
                            reward.target(), result.questId());

                default ->
                    log.warn("[QuestRewards] Unknown reward type: '{}'", reward.type());
            }
        }

        if (energy != 0 || health != 0 || stress != 0 || mood != 0
                || money != 0 || selfEsteem != 0) {
            session.player().applyStatChanges(
                    new StatChanges(energy, health, stress, mood, money, selfEsteem));
        }
    }

    private static String normaliseStat(String raw) {
        if (raw == null) return "";
        return raw.toLowerCase().replace("-", "").replace("_", "");
    }

    // ── context builders ─────────────────────────────────────────────────────────────

    private Map<String, String> buildNarrativeContext(GameSession session, String actionCode) {
        Map<String, String> ctx = new LinkedHashMap<>();
        ctx.put("actionCode", actionCode);
        ctx.put("day",        String.valueOf(session.time().day()));
        ctx.put("hour",       String.valueOf(session.time().hour()));
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

    /**
     * Quest objective context.
     *
     * Key naming matches ObjectiveSpec.target as produced by QuestSpecParser:
     *   - simple stats:       "energy", "mood", "work_days", ...
     *   - relationship fields: "NPC_ID:field"  e.g. "HUSBAND:closeness"
     *     (compound condition format from QuestSpecParser.parseStep)
     *   - action tracking:    "actionCode"
     *
     * work_days = consecutiveWorkDays, used by CAREER_GROWTH quest step 1
     * (objective: type=counter, target=work_days, threshold=5)
     */
    private Map<String, Object> buildQuestContext(GameSession session, String actionCode) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("actionCode", actionCode);
        ctx.put("day",        session.time().day());
        ctx.put("hour",       session.time().hour());
        ctx.put("energy",     session.player().stats().energy());
        ctx.put("health",     session.player().stats().health());
        ctx.put("stress",     session.player().stats().stress());
        ctx.put("mood",       session.player().stats().mood());
        ctx.put("money",      session.player().stats().money());
        ctx.put("selfEsteem", session.player().stats().selfEsteem());
        // work_days: consecutive days the player went to work
        // used by CAREER_GROWTH quest step 1 objective (target=work_days, threshold=5)
        ctx.put("work_days",  session.player().consecutiveWorkDays());
        // Relationship fields in "NPC_ID:field" format
        // Matches ObjectiveSpec.target for compound quest conditions
        session.relationships().all().forEach((npcId, rel) -> {
            ctx.put(npcId + ":closeness", rel.closeness());
            ctx.put(npcId + ":trust",     rel.trust());
            ctx.put(npcId + ":romance",   rel.romance());
            ctx.put(npcId + ":stability", rel.stability());
        });
        return ctx;
    }
}
