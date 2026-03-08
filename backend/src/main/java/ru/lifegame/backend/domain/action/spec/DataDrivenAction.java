package ru.lifegame.backend.domain.action.spec;

import ru.lifegame.backend.domain.action.*;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.*;

/**
 * Universal GameAction implementation driven entirely by PlayerActionSpec.
 * Replaces GoToWorkAction, DateWithHusbandAction, VisitFatherAction, etc.
 *
 * All behavior is determined by XML data — no game-specific logic in Java.
 */
public class DataDrivenAction implements GameAction {

    private final PlayerActionSpec spec;
    private final DataDrivenActionType actionType;

    public DataDrivenAction(PlayerActionSpec spec) {
        this.spec = spec;
        this.actionType = new DataDrivenActionType(spec.code(), spec.label(), spec.description());
    }

    @Override
    public ActionType type() {
        return actionType;
    }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        int base = spec.baseTimeCost();
        PlayerActionSpec.TimeCostSkillModifier mod = spec.timeCostSkillModifier();
        if (mod != null && session.player().skills() != null) {
            int skillLevel = session.player().skills().getLevel(mod.skillName());
            int reduction = (int) (skillLevel * mod.reductionPerLevel());
            return Math.max(base - reduction, mod.minCost());
        }
        return base;
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);

        PlayerActionSpec.StatEffects s = spec.stats();
        StatChanges statChanges = new StatChanges(
                s.energy(), s.health(), s.stress(),
                s.mood(), s.money(), s.selfEsteem()
        );

        String resultText = spec.resultTextTemplate()
                .replace("{timeCost}", String.valueOf(timeCost));

        PlayerActionSpec.ActionFlags f = spec.flags();

        // Build interactedNpcs: explicit flags + all relationship targets
        Set<String> interacted = new LinkedHashSet<>(f.interactedNpcs());
        spec.relationshipChanges().keySet().forEach(npc -> interacted.add(npc.toUpperCase()));
        spec.extraRelationshipEffects().forEach(e -> interacted.add(e.target().toUpperCase()));

        return new ActionResult(
                actionType,
                timeCost,
                resultText,
                statChanges,
                Map.copyOf(spec.relationshipChanges()),
                Map.copyOf(spec.petMoodChanges()),
                f.rested(),
                f.worked(),
                Set.copyOf(interacted)
        );
    }

    public PlayerActionSpec spec() {
        return spec;
    }
}
