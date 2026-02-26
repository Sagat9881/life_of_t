package ru.lifegame.backend.domain.conflict.types;

import ru.lifegame.backend.domain.conflict.core.*;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import java.util.Optional;

public enum HusbandConflicts implements ConflictType {
    HOUSEHOLD_DUTIES("HOUSEHOLD_DUTIES", "Домашние обязанности",
            "Муж недоволен распределением домашних дел"),
    LACK_OF_ATTENTION("LACK_OF_ATTENTION", "Нехватка внимания",
            "Муж чувствует, что ему не уделяют времени"),
    ROMANTIC_CRISIS("ROMANTIC_CRISIS", "Романтический кризис",
            "Романтика в отношениях угасает"),
    FINANCIAL_DISAGREEMENT("FINANCIAL_DISAGREEMENT", "Финансовые разногласия",
            "Разные взгляды на расходы");

    private final String code;
    private final String label;
    private final String description;

    HusbandConflicts(String code, String label, String description) {
        this.code = code; this.label = label; this.description = description;
    }

    @Override public String code() { return code; }
    @Override public String label() { return label; }
    @Override public String description() { return description; }
    @Override public Optional<NpcCode> opponent() { return Optional.of(NpcCode.HUSBAND); }
    @Override public ConflictCategory category() { return ConflictCategory.FAMILY; }
}
