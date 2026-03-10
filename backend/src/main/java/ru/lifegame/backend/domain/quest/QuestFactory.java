package ru.lifegame.backend.domain.quest;

import java.util.List;

public class QuestFactory {

    public static QuestLog createInitialQuestLog() {
        QuestLog log = new QuestLog();
        Quest career = createCareerGrowth();
        career.start();
        log.addQuest(career);
        Quest selfCare = createSelfCareArc();
        selfCare.start();
        log.addQuest(selfCare);
        Quest family = createFamilyHarmony();
        family.start();
        log.addQuest(family);
        return log;
    }

    private static Quest createCareerGrowth() {
        return new Quest("quest_career_growth", "CAREER_GROWTH",
                "\u041a\u0430\u0440\u044c\u0435\u0440\u043d\u044b\u0439 \u0440\u043e\u0441\u0442",
                "\u0414\u043e\u0431\u0438\u0442\u044c\u0441\u044f \u043f\u0440\u0438\u0437\u043d\u0430\u043d\u0438\u044f \u043d\u0430 \u0440\u0430\u0431\u043e\u0442\u0435",
                List.of(
                    new QuestStepState(new QuestObjective("counter", "work_days", 5,
                        "\u041e\u0442\u0440\u0430\u0431\u043e\u0442\u0430\u0442\u044c 5 \u0434\u043d\u0435\u0439"), 0),
                    new QuestStepState(new QuestObjective("threshold", "job_satisfaction", 70,
                        "\u0423\u0434\u043e\u0432\u043b\u0435\u0442\u0432\u043e\u0440\u0451\u043d\u043d\u043e\u0441\u0442\u044c \u0440\u0430\u0431\u043e\u0442\u043e\u0439 >= 70"), 0),
                    new QuestStepState(new QuestObjective("threshold", "job_satisfaction", 80,
                        "\u0423\u0434\u043e\u0432\u043b\u0435\u0442\u0432\u043e\u0440\u0451\u043d\u043d\u043e\u0441\u0442\u044c \u0440\u0430\u0431\u043e\u0442\u043e\u0439 >= 80"), 0)
                ));
    }

    private static Quest createSelfCareArc() {
        return new Quest("quest_self_care", "SELF_CARE_ARC",
                "\u0417\u0430\u0431\u043e\u0442\u0430 \u043e \u0441\u0435\u0431\u0435",
                "\u041d\u0430\u0443\u0447\u0438\u0442\u044c\u0441\u044f \u0437\u0430\u0431\u043e\u0442\u0438\u0442\u044c\u0441\u044f \u043e \u0441\u0435\u0431\u0435",
                List.of(
                    new QuestStepState(new QuestObjective("counter", "self_care_count", 3,
                        "\u041f\u043e\u0437\u0430\u0431\u043e\u0442\u0438\u0442\u044c\u0441\u044f \u043e \u0441\u0435\u0431\u0435 3 \u0440\u0430\u0437\u0430"), 0),
                    new QuestStepState(new QuestObjective("threshold", "self_esteem", 75,
                        "\u0421\u0430\u043c\u043e\u043e\u0446\u0435\u043d\u043a\u0430 >= 75"), 0),
                    new QuestStepState(new QuestObjective("compound", "mood_and_esteem", 80,
                        "\u041d\u0430\u0441\u0442\u0440\u043e\u0435\u043d\u0438\u0435 >= 80 \u0438 \u0441\u0430\u043c\u043e\u043e\u0446\u0435\u043d\u043a\u0430 >= 80"), 0)
                ));
    }

    private static Quest createFamilyHarmony() {
        return new Quest("quest_family_harmony", "FAMILY_HARMONY",
                "\u0421\u0435\u043c\u0435\u0439\u043d\u0430\u044f \u0433\u0430\u0440\u043c\u043e\u043d\u0438\u044f",
                "\u0423\u043a\u0440\u0435\u043f\u0438\u0442\u044c \u0441\u0435\u043c\u0435\u0439\u043d\u044b\u0435 \u043e\u0442\u043d\u043e\u0448\u0435\u043d\u0438\u044f",
                List.of(
                    new QuestStepState(new QuestObjective("counter", "date_count", 3,
                        "\u0421\u0445\u043e\u0434\u0438\u0442\u044c \u043d\u0430 \u0441\u0432\u0438\u0434\u0430\u043d\u0438\u0435 3 \u0440\u0430\u0437\u0430"), 0),
                    new QuestStepState(new QuestObjective("counter", "visit_count", 3,
                        "\u041d\u0430\u0432\u0435\u0441\u0442\u0438\u0442\u044c \u043e\u0442\u0446\u0430 3 \u0440\u0430\u0437\u0430"), 0),
                    new QuestStepState(new QuestObjective("compound", "family_closeness", 80,
                        "\u0411\u043b\u0438\u0437\u043e\u0441\u0442\u044c \u0441 \u043c\u0443\u0436\u0435\u043c >= 80, \u0440\u043e\u043c\u0430\u043d\u0442\u0438\u043a\u0430 >= 70"), 0)
                ));
    }
}
