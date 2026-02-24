package ru.lifegame.backend.domain.ending;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.*;
import ru.lifegame.backend.domain.quest.QuestLog;
import ru.lifegame.backend.domain.quest.QuestType;

import java.util.Optional;

public class EndingEvaluator {

    public Optional<Ending> findBestEnding(PlayerCharacter player, Relationships relationships,
                                            Pets pets, QuestLog questLog, GameTime time) {
        if (time.day() < GameBalance.MAX_GAME_DAYS) {
            return Optional.empty();
        }
        if (player.job().satisfaction() >= 80
                && questLog.hasCompletedQuest(QuestType.CAREER_GROWTH)) {
            return Optional.of(new Ending(EndingType.GOOD_CAREER, EndingCategory.STORY_ENDING,
                    "Карьерный взлёт", "Татьяна стала востребованным специалистом.",
                    "Её проекты признаны лучшими в индустрии."));
        }
        if (relationships.get(NpcCode.HUSBAND) != null
                && !relationships.get(NpcCode.HUSBAND).broken()
                && relationships.get(NpcCode.HUSBAND).closeness() >= 80
                && relationships.get(NpcCode.HUSBAND).romance() >= 70) {
            return Optional.of(new Ending(EndingType.FAMILY_HAPPINESS, EndingCategory.STORY_ENDING,
                    "Семейное счастье", "Семья стала крепче и дружнее.",
                    "Татьяна нашла баланс между работой и семьёй."));
        }
        if (questLog.hasCompletedQuest(QuestType.SELF_CARE_ARC)
                && player.stats().mood() >= 70
                && player.stats().selfEsteem() >= 70) {
            return Optional.of(new Ending(EndingType.BALANCED_LIFE, EndingCategory.STORY_ENDING,
                    "Гармония", "Татьяна научилась заботиться о себе.",
                    "Жизнь обрела баланс и смысл."));
        }
        return Optional.of(new Ending(EndingType.NEUTRAL_EPILOGUE, EndingCategory.NEUTRAL_EPILOGUE,
                "Жизнь продолжается", "30 дней позади. Жизнь идёт своим чередом.",
                "Татьяна продолжает свой путь."));
    }
}
