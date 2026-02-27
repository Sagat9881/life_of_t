package ru.lifegame.backend.domain.model.pet;

public enum PetCode {
    BARSIK("barsik", "Барсик"),
    SAM("sam", "Сэм");

    private final String code;
    private final String name;

    PetCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }
}
