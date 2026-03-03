package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.ending.Ending;

import static ru.lifegame.backend.domain.ending.EndingCategory.GAME_OVER_ENDING;
import static ru.lifegame.backend.domain.ending.EndingType.LONELY_CAT_LADY;
import static ru.lifegame.backend.domain.ending.EndingType.TOTAL_BURNOUT;

/**
 * Enumeration of possible game over reasons.
 */
public enum GameOverReason {
    BURNOUT(new Ending(TOTAL_BURNOUT, GAME_OVER_ENDING,
            "Вы погибли от изнурения",
            "Нужно было меньше работать",
            "Отдохните и выпейте чашечку чая перед следующей игровой сессией :3")),
    DIVORCE(new Ending(LONELY_CAT_LADY, GAME_OVER_ENDING,
            "Вы расстались",
            "Стоило больше ценить все радостные моменты жизни",
            "Вы не знали, что такое \"время\". Не переживайте, в следующий раз всё получится. Попробуйте ещё раз.")),
    ISOLATION(new Ending(LONELY_CAT_LADY, GAME_OVER_ENDING,
            "Вы остались совсем одни",
            "Сердце не бьётся, но и не болит",
            "Вы не знали, что такое \"время\". Не переживайте, в следующий раз всё получится. Попробуйте ещё раз.")),
    PET_DEATH(new Ending(TOTAL_BURNOUT, GAME_OVER_ENDING,
            "Питомец погиб",
            "За ними нужно следить...",
            "Уделяйте больше внимания питомцам в следующий раз.")),
    BANKRUPTCY(new Ending(TOTAL_BURNOUT, GAME_OVER_ENDING,
            "Банкротство",
            "Деньги закончились",
            "Нужно было следить за финансами. Попробуйте ещё раз."));

    private final Ending ending;

    GameOverReason(Ending ending) {
        this.ending = ending;
    }

    public Ending ending() {
        return ending;
    }
}
