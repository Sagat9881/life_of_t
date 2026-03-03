package ru.lifegame.backend.domain.conflict.triggers;

import ru.lifegame.backend.domain.conflict.core.Conflict;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConflictTriggers {
    private final List<ConflictTrigger> triggers;

    public ConflictTriggers() {
        this.triggers = new ArrayList<>();
        registerDefaultTriggers();
    }

    public ConflictTriggers(List<ConflictTrigger> customTriggers) {
        this.triggers = new ArrayList<>(customTriggers);
    }

    private void registerDefaultTriggers() {
        triggers.add(new HouseholdDutiesTrigger());
        triggers.add(new LackOfAttentionTrigger());
        triggers.add(new RomanticCrisisTrigger());
        triggers.add(new FinancialDisagreementTrigger());
        triggers.add(new FatherNeglectedTrigger());
        triggers.add(new FatherCriticismTrigger());
        triggers.add(new FatherConcernTrigger());
        triggers.add(new IdentityCrisisTrigger());
        triggers.add(new BurnoutTrigger());
        triggers.add(new GuiltTrigger());
    }

    public void addTrigger(ConflictTrigger trigger) { triggers.add(trigger); }

    public void removeTrigger(Class<? extends ConflictTrigger> triggerClass) {
        triggers.removeIf(t -> t.getClass().equals(triggerClass));
    }

    public List<Conflict> checkTriggers(PlayerCharacter player, Relationships relationships, GameTime time) {
        return triggers.stream()
            .map(trigger -> trigger.check(player, relationships, time))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
