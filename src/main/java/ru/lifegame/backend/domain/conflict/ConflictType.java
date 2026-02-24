package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.model.NpcCode;
import java.util.Optional;

public interface ConflictType {
    String code();
    String label();
    String description();
    Optional<NpcCode> opponent();
    ConflictCategory category();
}
