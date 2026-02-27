package ru.lifegame.backend.domain.conflict.tactics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry for looking up ConflictTactic instances by their code.
 */
public class ConflictTacticRegistry {
    private static final Map<String, ConflictTactic> TACTICS = new HashMap<>();

    static {
        // Register all base tactics
        for (BaseConflictTactics tactic : BaseConflictTactics.values()) {
            register(tactic);
        }
        
        // Register all skill-based tactics
        for (SkillBasedConflictTactics tactic : SkillBasedConflictTactics.values()) {
            register(tactic);
        }
    }

    private static void register(ConflictTactic tactic) {
        TACTICS.put(tactic.code().toUpperCase(), tactic);
    }

    /**
     * Find a tactic by its code (case-insensitive).
     */
    public static Optional<ConflictTactic> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(TACTICS.get(code.toUpperCase()));
    }

    /**
     * Get a tactic by code or throw exception if not found.
     */
    public static ConflictTactic getByCode(String code) {
        return findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Unknown tactic code: " + code));
    }
}
