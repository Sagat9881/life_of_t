package ru.lifegame.backend.domain.action;

public enum Actions implements ActionType {
    GO_TO_WORK("GO_TO_WORK", "Пойти на работу", "Работать над проектом"),
    VISIT_FATHER("VISIT_FATHER", "Поехать к отцу", "Навестить отца"),
    DATE_WITH_HUSBAND("DATE_WITH_HUSBAND", "Свидание с мужем", "Романтический вечер"),
    PLAY_WITH_CAT("PLAY_WITH_CAT", "Поиграть с Гарфилдом", "Время с котом"),
    WALK_DOG("WALK_DOG", "Погулять с Сэмом", "Прогулка с собакой"),
    SELF_CARE("SELF_CARE", "Заняться собой", "Время для себя"),
    REST_AT_HOME("REST_AT_HOME", "Отдохнуть дома", "Отдых и восстановление");

    private final String code;
    private final String label;
    private final String description;

    Actions(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    @Override public String code() { return code; }
    @Override public String label() { return label; }
    @Override public String description() { return description; }
}
