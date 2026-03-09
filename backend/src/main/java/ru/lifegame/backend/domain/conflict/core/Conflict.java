package ru.lifegame.backend.domain.conflict.core;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Runtime domain object representing an active conflict instance.
 * Metadata comes from ConflictSpec (loaded from conflicts.xml).
 */
public class Conflict {
    private final String id;
    private final String conflictId;     // e.g. "BURNOUT"
    private final String label;          // e.g. "Профессиональное выгорание"
    private final String description;    // e.g. "Работа поглощает все силы"
    private final String opponentId;     // e.g. "HUSBAND", "FATHER", "SELF"
    private final String category;       // e.g. "internal", "relationship"
    private ConflictStage stage;
    private ConflictStressPoints csp;
    private final List<ConflictRound> rounds;
    private ConflictResolution resolution;

    public Conflict(String id, String conflictId, String label, String description,
                    String opponentId, String category) {
        this.id = id;
        this.conflictId = conflictId;
        this.label = label;
        this.description = description;
        this.opponentId = opponentId;
        this.category = category;
        this.stage = ConflictStage.BREWING;
        this.csp = ConflictStressPoints.initial();
        this.rounds = new ArrayList<>();
        this.resolution = null;
    }

    /**
     * Factory: create runtime Conflict from XML-loaded ConflictSpec.
     */
    public static Conflict fromSpec(ConflictSpec spec) {
        return new Conflict(
            UUID.randomUUID().toString(),
            spec.id(),
            spec.meta().label(),
            spec.meta().description(),
            spec.meta().opponentId(),
            spec.meta().category()
        );
    }

    public void escalate() {
        if (stage == ConflictStage.BREWING) {
            stage = ConflictStage.ESCALATION;
        }
    }

    public void avoidAtBrewingStage() {
        if (stage == ConflictStage.BREWING) {
            this.resolution = ConflictResolution.avoided();
            this.stage = ConflictStage.RESOLUTION;
        }
    }

    private String buildSituation(int round) {
        return switch (stage) {
            case ESCALATION -> label + ": напряжение нарастает (раунд " + round + ")";
            case CLIMAX -> label + ": решающий момент!";
            default -> label + ": разговор";
        };
    }

    public boolean isResolved() {
        return stage == ConflictStage.RESOLUTION && resolution != null;
    }

    public Optional<String> opponent() {
        if (opponentId == null || opponentId.equals("SELF")) {
            return Optional.empty();
        }
        return Optional.of(opponentId);
    }

    // --- Getters ---
    public String id() { return id; }
    public String conflictId() { return conflictId; }
    public String label() { return label; }
    public String description() { return description; }
    public String opponentId() { return opponentId; }
    public String category() { return category; }
    public ConflictStage stage() { return stage; }
    public ConflictStressPoints csp() { return csp; }
    public List<ConflictRound> rounds() { return List.copyOf(rounds); }
    public ConflictResolution resolution() { return resolution; }
}
