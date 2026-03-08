package ru.lifegame.backend.domain.ending;

import ru.lifegame.backend.domain.balance.GameBalance;
import ru.lifegame.backend.domain.model.character.PlayerCharacter;
import ru.lifegame.backend.domain.model.pet.Pets;
import ru.lifegame.backend.domain.model.relationship.NpcCode;
import ru.lifegame.backend.domain.model.relationship.Relationship;
import ru.lifegame.backend.domain.model.relationship.Relationships;
import ru.lifegame.backend.domain.model.session.GameTime;
import ru.lifegame.backend.domain.quest.QuestLog;
import ru.lifegame.backend.domain.quest.QuestType;

import java.util.Optional;

public class EndingEvaluator {

    public Optional<Ending> findBestEnding(
            PlayerCharacter player,
            Relationships relationships,
            Pets pets,
            QuestLog questLog,
            GameTime time
    ) {
        if (time.day() < GameBalance.MAX_GAME_DAYS) {
            return Optional.empty();
        }
        if (player.job().satisfaction() >= 80
                && questLog.hasCompletedQuest(QuestType.CAREER_GROWTH)) {
            return Optional.of(new Ending(EndingType.GOOD_CAREER, EndingCategory.STORY_ENDING,
                    "\u041a\u0430\u0440\u044c\u0435\u0440\u043d\u044b\u0439 \u0432\u0437\u043b\u0451\u0442", "\u0422\u0430\u0442\u044c\u044f\u043d\u0430 \u0441\u0442\u0430\u043b\u0430 \u0432\u043e\u0441\u0442\u0440\u0435\u0431\u043e\u0432\u0430\u043d\u043d\u044b\u043c \u0441\u043f\u0435\u0446\u0438\u0430\u043b\u0438\u0441\u0442\u043e\u043c.",
                    "\u0415\u0451 \u043f\u0440\u043e\u0435\u043a\u0442\u044b \u043f\u0440\u0438\u0437\u043d\u0430\u043d\u044b \u043b\u0443\u0447\u0448\u0438\u043c\u0438 \u0432 \u0438\u043d\u0434\u0443\u0441\u0442\u0440\u0438\u0438."));
        }
        Relationship husband = relationships.get(NpcCode.HUSBAND);
        if (husband != null
                && !husband.broken()
                && husband.closeness() >= 80
                && husband.romance() >= 70) {
            return Optional.of(new Ending(EndingType.FAMILY_HAPPINESS, EndingCategory.STORY_ENDING,
                    "\u0421\u0435\u043c\u0435\u0439\u043d\u043e\u0435 \u0441\u0447\u0430\u0441\u0442\u044c\u0435", "\u0421\u0435\u043c\u044c\u044f \u0441\u0442\u0430\u043b\u0430 \u043a\u0440\u0435\u043f\u0447\u0435 \u0438 \u0434\u0440\u0443\u0436\u043d\u0435\u0435.",
                    "\u0422\u0430\u0442\u044c\u044f\u043d\u0430 \u043d\u0430\u0448\u043b\u0430 \u0431\u0430\u043b\u0430\u043d\u0441 \u043c\u0435\u0436\u0434\u0443 \u0440\u0430\u0431\u043e\u0442\u043e\u0439 \u0438 \u0441\u0435\u043c\u044c\u0451\u0439."));
        }
        if (questLog.hasCompletedQuest(QuestType.SELF_CARE_ARC)
                && player.stats().mood() >= 70
                && player.stats().selfEsteem() >= 70) {
            return Optional.of(new Ending(EndingType.BALANCED_LIFE, EndingCategory.STORY_ENDING,
                    "\u0413\u0430\u0440\u043c\u043e\u043d\u0438\u044f", "\u0422\u0430\u0442\u044c\u044f\u043d\u0430 \u043d\u0430\u0443\u0447\u0438\u043b\u0430\u0441\u044c \u0437\u0430\u0431\u043e\u0442\u0438\u0442\u044c\u0441\u044f \u043e \u0441\u0435\u0431\u0435.",
                    "\u0416\u0438\u0437\u043d\u044c \u043e\u0431\u0440\u0435\u043b\u0430 \u0431\u0430\u043b\u0430\u043d\u0441 \u0438 \u0441\u043c\u044b\u0441\u043b."));
        }
        return Optional.of(new Ending(EndingType.NEUTRAL_EPILOGUE, EndingCategory.NEUTRAL_EPILOGUE,
                "\u0416\u0438\u0437\u043d\u044c \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0430\u0435\u0442\u0441\u044f", "30 \u0434\u043d\u0435\u0439 \u043f\u043e\u0437\u0430\u0434\u0438. \u0416\u0438\u0437\u043d\u044c \u0438\u0434\u0451\u0442 \u0441\u0432\u043e\u0438\u043c \u0447\u0435\u0440\u0435\u0434\u043e\u043c.",
                "\u0422\u0430\u0442\u044c\u044f\u043d\u0430 \u043f\u0440\u043e\u0434\u043e\u043b\u0436\u0430\u0435\u0442 \u0441\u0432\u043e\u0439 \u043f\u0443\u0442\u044c."));
    }
}
