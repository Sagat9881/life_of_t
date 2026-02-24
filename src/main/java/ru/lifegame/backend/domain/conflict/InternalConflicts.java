package ru.lifegame.backend.domain.conflict;

import ru.lifegame.backend.domain.model.NpcCode;
import java.util.Optional;

public enum InternalConflicts implements ConflictType {
    IDENTITY_CRISIS("IDENTITY_CRISIS", "Кризис самоопределения",
            "Татьяна не уверена в своих жизненных выборах"),
    BURNOUT("BURNOUT", "Выгорание",
            "Татьяна на грани эмоционального выгорания"),
    GUILT("GUILT", "Чувство вины",
            "Татьяна чувствует вину перед близкими");

    private final String code;
    private final String label;
    private final String description;

    InternalConflicts(String code, String label, String description) {
        this.code = code; this.label = label; this.description = description;
    }

    @Override public String code() { return code; }
    @Override public String label() { return label; }
    @Override public String description() { return description; }
    @Override public Optional<NpcCode> opponent() { return Optional.empty(); }
    @Override public ConflictCategory category() { return ConflictCategory.INTERNAL; }
}
