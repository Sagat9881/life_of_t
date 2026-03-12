package ru.lifegame.backend.domain.action.spec;

import ru.lifegame.backend.domain.action.ActionResult;
import ru.lifegame.backend.domain.action.ActionType;
import ru.lifegame.backend.domain.action.GameAction;
import ru.lifegame.backend.domain.action.GameSessionReadModel;
import ru.lifegame.backend.domain.model.stats.StatChanges;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataDrivenAction implements GameAction {

    private final PlayerActionSpec spec;
    private final ActionType actionType;
    private final boolean rested;
    private final boolean workedToday;
    private final Set<String> interactedNpcs;

    public DataDrivenAction(PlayerActionSpec spec) {
        this.spec = spec;
        this.actionType = new DataDrivenActionType(
                spec.code(),
                spec.label(),
                spec.description()
        );
        this.rested      = spec.tags().contains("rest");
        this.workedToday = spec.tags().contains("work");
        this.interactedNpcs = spec.relationshipChanges().keySet()
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toUnmodifiableSet());
    }

    public PlayerActionSpec spec() {
        return spec;
    }

    @Override
    public ActionType type() {
        return actionType;
    }

    @Override
    public int calculateTimeCost(GameSessionReadModel session) {
        return spec.baseTimeCost();
    }

    @Override
    public ActionResult calculate(GameSessionReadModel session) {
        int timeCost = calculateTimeCost(session);
        StatChanges statChanges = new StatChanges(
                spec.stats().energy(),
                spec.stats().health(),
                spec.stats().stress(),
                spec.stats().mood(),
                spec.stats().money(),
                spec.stats().selfEsteem()
        );
        return new ActionResult(
                actionType,
                timeCost,
                spec.description(),
                statChanges,
                Map.copyOf(spec.relationshipChanges()),
                Map.copyOf(spec.petMoodChanges()),
                rested,
                workedToday,
                interactedNpcs
        );
    }
}
