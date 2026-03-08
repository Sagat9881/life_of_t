package ru.lifegame.backend.domain.engine.spec;

public record ConditionSpec(
    String type,
    String target,
    String operator,
    String value
) {
    public boolean isType(String t) {
        return type != null && type.equalsIgnoreCase(t);
    }
}
