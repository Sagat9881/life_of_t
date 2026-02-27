package ru.lifegame.backend.domain.action;

public enum StandardActionType implements ActionType {
    WORK("work", "Работа", "Работать над проектами"),
    VISIT_FATHER("visit_father", "Навестить отца", "Провести время с отцом"),
    DATE_HUSBAND("date_husband", "Свидание с мужем", "Романтическое свидание"),
    PLAY_CAT("play_cat", "Играть с котом", "Поиграть с Барсиком"),
    WALK_DOG("walk_dog", "Гулять с собакой", "Выгулять Сэма"),
    SELF_CARE("self_care", "Забота о себе", "Время для себя"),
    REST("rest", "Отдых", "Отдохнуть и восстановить силы"),
    HOUSEHOLD("household", "Домашние дела", "Уборка, готовка");

    private final String code;
    private final String label;
    private final String description;

    StandardActionType(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public String description() {
        return description;
    }
}
