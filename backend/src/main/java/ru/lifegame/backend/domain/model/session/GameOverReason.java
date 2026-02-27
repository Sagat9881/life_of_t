package ru.lifegame.backend.domain.model.session;

import ru.lifegame.backend.domain.ending.Ending;

import static ru.lifegame.backend.domain.ending.EndingCategory.GAME_OVER_ENDING;
import static ru.lifegame.backend.domain.ending.EndingType.LONELY_CAT_LADY;
import static ru.lifegame.backend.domain.ending.EndingType.TOTAL_BURNOUT;

/**
 * Enumeration of possible game over reasons.
 */
public enum GameOverReason {
    BURNOUT(new Ending(TOTAL_BURNOUT, GAME_OVER_ENDING,"Вы погибли от изнурения","Нужно было меньше работать", "Отдохните и выпейти чашечку чая перед следующей игровой ссессией :3")),
    DIVORCE(new Ending(LONELY_CAT_LADY, GAME_OVER_ENDING,"Вы расстались","Стоило больше ценить все радостные моменты жизни","Вы не знали, что такое \"время\". Не переживайте, в следующий раз все получится. Попробуйте еще раз.")),
    ISOLATION(new Ending(LONELY_CAT_LADY, GAME_OVER_ENDING,"Вы остались совсем одни"," Сердце не бьется, но и не болит", "Вы не знали, что такое \"время\". Не переживайте, в следующий раз все получится. Попробуйте еще раз.")),
    PET_DEATH(new Ending(TOTAL_BURNOUT, GAME_OVER_ENDING,"","", "")),
    BANKRUPTCY(new Ending(TOTAL_BURNOUT, GAME_OVER_ENDING,"","", ""));

    private final Ending ending;

    GameOverReason(Ending ending) {
        this.ending = ending;
    }

    public Ending ending(){return ending;};
}


