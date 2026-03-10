package ru.lifegame.backend.domain.conflict.core;

import ru.lifegame.backend.domain.conflict.spec.ConflictSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Runtime domain object representing an active conflict instance.
 * Metadata comes from ConflictSpec (loaded from conflicts XML).
 */
public class Conflict {
    private final String id;
    private final String conflictId;
    private final String label;
    private final String description;
    private final String opponentId;
    private final String category;
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

    /**
     * Update CSP after a tactic is applied.
     */
    public void updateCsp(ConflictStressPoints updated) {
        this.csp = updated;
        if (stage == ConflictStage.BREWING) {
            this.stage = ConflictStage.ESCALATION;
        }
    }

    /**
     * Finalize the conflict with a resolution.
     */
    public void resolve(ConflictResolution resolution) {
        this.resolution = resolution;
        this.stage = ConflictStage.RESOLUTION;
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
