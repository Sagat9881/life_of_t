package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.conflict.triggers.*;
import ru.lifegame.backend.domain.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain service that evaluates conflict triggers using strategy pattern.
 * Triggers can be dynamically added or removed from the registry.
 * Part of the conflict subdomain.
 */
public class ConflictTriggers {
    private final List<ConflictTrigger> triggers;

    public ConflictTriggers() {
        this.triggers = new ArrayList<>();
        registerDefaultTriggers();
    }

    public ConflictTriggers(List<ConflictTrigger> customTriggers) {
        this.triggers = new ArrayList<>(customTriggers);
    }

    /**
     * Register default game triggers.
     */
    private void registerDefaultTriggers() {
        // Husband conflicts
        triggers.add(new HouseholdDutiesTrigger());
        triggers.add(new LackOfAttentionTrigger());
        triggers.add(new RomanticCrisisTrigger());
        triggers.add(new FinancialDisagreementTrigger());
        
        // Father conflicts
        triggers.add(new FatherNeglectedTrigger());
        triggers.add(new FatherCriticismTrigger());
        triggers.add(new FatherConcernTrigger());
        
        // Internal conflicts
        triggers.add(new IdentityCrisisTrigger());
        triggers.add(new BurnoutTrigger());
        triggers.add(new GuiltTrigger());
    }

    /**
     * Add a custom trigger dynamically.
     */
    public void addTrigger(ConflictTrigger trigger) {
        triggers.add(trigger);
    }

    /**
     * Remove a trigger by class type.
     */
    public void removeTrigger(Class<? extends ConflictTrigger> triggerClass) {
        triggers.removeIf(t -> t.getClass().equals(triggerClass));
    }

    /**
     * Check all registered triggers and return conflicts that should be activated.
     */
    public List<Conflict> checkTriggers(PlayerCharacter player, Relationships relationships, GameTime time) {
        return triggers.stream()
            .map(trigger -> trigger.check(player, relationships, time))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }
}
