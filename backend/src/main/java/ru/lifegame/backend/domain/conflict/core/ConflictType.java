package ru.lifegame.backend.domain.conflict.core;

import java.util.Optional;

public interface ConflictType {
    String code();
    String label();
    String description();
    Optional<String> opponent();
    ConflictCategory category();
}
