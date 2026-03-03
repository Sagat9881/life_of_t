package ru.lifegame.backend.domain.conflict.tactics;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConflictTacticRegistry {
    private static final Map<String, ConflictTactic> TACTICS = new HashMap<>();

    static {
        for (BaseConflictTactics tactic : BaseConflictTactics.values()) {
            TACTICS.put(tactic.code().toUpperCase(), tactic);
        }
        for (SkillBasedConflictTactics tactic : SkillBasedConflictTactics.values()) {
            TACTICS.put(tactic.code().toUpperCase(), tactic);
        }
    }

    public static Optional<ConflictTactic> findByCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(TACTICS.get(code.toUpperCase()));
    }

    public static ConflictTactic getByCode(String code) {
        return findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Unknown tactic code: " + code));
    }
}
