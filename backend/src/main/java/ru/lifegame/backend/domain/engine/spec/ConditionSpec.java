package ru.lifegame.backend.domain.engine.spec;

public record ConditionSpec(
    String type,
    String target,
    String operator,
    String value
) {
    public int intValue() {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return 0; }
    }
    public double doubleValue() {
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) { return 0.0; }
    }
}
