package ru.lifegame.backend.domain.model.pet;

public enum PetCode {
    BARSIK("barsik", "Барсик"),
    SAM("sam", "Сэм");

    private final String code;
    private final String displayName;

    PetCode(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String code() {
        return code;
    }

    public String displayName() {
        return displayName;
    }
}
