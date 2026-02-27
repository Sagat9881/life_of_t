package ru.lifegame.backend.domain.conflict.types;

import ru.lifegame.backend.domain.conflict.core.ConflictCategory;
import ru.lifegame.backend.domain.conflict.core.ConflictType;
import ru.lifegame.backend.domain.model.relationship.NpcCode;

import java.util.Optional;

public enum FatherConflicts implements ConflictType {
    FEELING_NEGLECTED("FEELING_NEGLECTED", "Чувствует себя забытым",
            "Отец обижен, что дочь его не навещает"),
    CRITICISM_OF_CHOICES("CRITICISM_OF_CHOICES", "Критика выбора",
            "Отец критикует жизненные решения"),
    CONCERN_FOR_WELLBEING("CONCERN_FOR_WELLBEING", "Беспокойство о благополучии",
            "Отец переживает за финансы и здоровье дочери");

    private final String code;
    private final String label;
    private final String description;

    FatherConflicts(String code, String label, String description) {
        this.code = code; this.label = label; this.description = description;
    }

    @Override public String code() { return code; }
    @Override public String label() { return label; }
    @Override public String description() { return description; }
    @Override public Optional<NpcCode> opponent() { return Optional.of(NpcCode.FATHER); }
    @Override public ConflictCategory category() { return ConflictCategory.FAMILY; }
}
