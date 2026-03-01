package ru.lifegame.backend.domain.action;

public enum StandardActionType implements ActionType {
    // Existing actions
    WORK("work", "Работа", "Работать над проектами"),
    VISIT_FATHER("visit_father", "Навестить отца", "Провести время с отцом"),
    DATE_HUSBAND("date_husband", "Свидание с мужем", "Романтическое свидание"),
    PLAY_CAT("play_cat", "Играть с котом", "Поиграть с Барсиком"),
    WALK_DOG("walk_dog", "Гулять с собакой", "Выгулять Сэма"),
    SELF_CARE("self_care", "Забота о себе", "Время для себя"),
    REST("rest", "Отдых", "Отдохнуть и восстановить силы"),
    HOUSEHOLD("household", "Домашние дела", "Уборка, готовка"),

    // RoomPage actions
    CALL_HUSBAND("call_husband", "Позвонить мужу", "Позвонить Александру и поболтать"),
    REST_AT_HOME("rest_at_home", "Отдых дома", "Отдохнуть на кровати"),
    WATCH_TV("watch_tv", "Смотреть ТВ", "Посмотреть сериал"),
    PLAY_WITH_PET("play_with_pet", "Играть с питомцем", "Поиграть с Гарфилдом"),

    // OfficePage actions
    WORK_ON_PROJECT("work_on_project", "Работа над проектом", "Работать над дизайн-проектом"),
    MAKE_COFFEE("make_coffee", "Сделать кофе", "Перерыв на кофе"),
    TALK_TO_COLLEAGUE("talk_to_colleague", "Поговорить с коллегой", "Обсудить рабочие вопросы"),

    // ParkPage actions
    REST_ON_BENCH("rest_on_bench", "Отдохнуть на скамейке", "Присесть и подышать свежим воздухом"),
    FEED_DUCKS("feed_ducks", "Покормить уток", "Покрошить хлеб уткам"),
    JOGGING("jogging", "Пробежка", "Утренняя пробежка по парку"),
    WALK_DOG_PARK("walk_dog_park", "Прогулка с Сэмом", "Выгулять Сэма в парке");

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
