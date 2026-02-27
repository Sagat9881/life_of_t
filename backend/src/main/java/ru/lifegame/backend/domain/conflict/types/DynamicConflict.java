package ru.lifegame.backend.domain.conflict.types;

import ru.lifegame.backend.domain.conflict.core.ConflictCategory;
import ru.lifegame.backend.domain.conflict.core.ConflictType;
import ru.lifegame.backend.domain.model.relationship.NpcCode;

import java.util.Optional;
import java.util.UUID;

public class DynamicConflict implements ConflictType {
    private final String code;
    private final String label;
    private final String description;
    private final String dynamicOpponentName;
    private final ConflictCategory category;

    public DynamicConflict(String label, String description,
                           String dynamicOpponentName, ConflictCategory category) {
        this.code = "DYN_" + UUID.randomUUID().toString().substring(0, 8);
        this.label = label;
        this.description = description;
        this.dynamicOpponentName = dynamicOpponentName;
        this.category = category;
    }

    @Override public String code() { return code; }
    @Override public String label() { return label; }
    @Override public String description() { return description; }
    @Override public Optional<NpcCode> opponent() { return Optional.empty(); }
    @Override public ConflictCategory category() { return category; }
    public String dynamicOpponentName() { return dynamicOpponentName; }
}
