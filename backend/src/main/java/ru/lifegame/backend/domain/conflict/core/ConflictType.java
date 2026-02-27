package ru.lifegame.backend.domain.conflict.core;

import ru.lifegame.backend.domain.model.relationship.NpcCode;

import java.util.Optional;

public interface ConflictType {
    String code();
    String label();
    String description();
    Optional<NpcCode> opponent();
    ConflictCategory category();
}
