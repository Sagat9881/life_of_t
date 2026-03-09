package ru.lifegame.backend.application.service;

import org.springframework.stereotype.Service;
import ru.lifegame.backend.domain.dto.content.*;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for loading and caching game content from XML specs.
 * TODO: Integrate with actual XML parsers from narrative module.
 * For now returns placeholder data to enable frontend development.
 */
@Service
public class GameContentService {

    private final Map<String, ActionDefView> actions = new ConcurrentHashMap<>();
    private final Map<String, ConflictDefView> conflicts = new ConcurrentHashMap<>();
    private final Map<String, QuestDefView> quests = new ConcurrentHashMap<>();
    private final Map<String, EventDefView> events = new ConcurrentHashMap<>();
    
    private ContentVersion currentVersion;

    @PostConstruct
    public void initialize() {
        loadContent();
    }

    private void loadContent() {
        // TODO: Parse narrative XML files
        // For now, load hardcoded content to unblock frontend
        loadPlaceholderActions();
        loadPlaceholderConflicts();
        loadPlaceholderQuests();
        loadPlaceholderEvents();
        
        currentVersion = new ContentVersion("1.0.0", Instant.now());
    }

    public List<ActionDefView> getAllActions() {
        return List.copyOf(actions.values());
    }

    public List<ConflictDefView> getAllConflicts() {
        return List.copyOf(conflicts.values());
    }

    public List<QuestDefView> getAllQuests() {
        return List.copyOf(quests.values());
    }

    public List<EventDefView> getAllEvents() {
        return List.copyOf(events.values());
    }

    public ContentVersion getCurrentVersion() {
        return currentVersion;
    }

    // ============ Placeholder data loaders ============

    private void loadPlaceholderActions() {
        actions.put("WORK", new ActionDefView(
            "WORK",
            "Работа",
            "Поработать в офисе",
            List.of("career", "income"),
            30, // energyCost
            30, // minEnergy
            Map.of(),
            List.of(),
            List.of(),
            Map.of("energy", -30, "stress", 10, "mood", -5),
            Map.of("professional", 2),
            1000, // moneyGain
            240, // 4 hours
            "work",
            "briefcase",
            List.of("morning", "day"),
            List.of("workplace"),
            List.of("work_conflict"),
            List.of("career_quest_1")
        ));

        actions.put("DATE_WITH_HUSBAND", new ActionDefView(
            "DATE_WITH_HUSBAND",
            "Свидание с мужем",
            "Провести время с Сэмом",
            List.of("relationship", "romance"),
            20,
            20,
            Map.of(),
            List.of(),
            List.of(),
            Map.of("energy", -20, "stress", -15, "mood", 20),
            Map.of("empathy", 1, "communication", 1),
            -500, // costs money
            120,
            "date",
            "heart",
            List.of("evening"),
            List.of("home", "cafe", "park"),
            List.of("relationship_conflict"),
            List.of("romance_quest_1")
        ));

        actions.put("REST", new ActionDefView(
            "REST",
            "Отдохнуть",
            "Отдохнуть и восстановить силы",
            List.of("self_care"),
            0,
            0,
            Map.of(),
            List.of(),
            List.of(),
            Map.of("energy", 30, "stress", -10, "mood", 10),
            Map.of(),
            0,
            60,
            "rest",
            "bed",
            List.of("morning", "day", "evening", "night"),
            List.of("home"),
            List.of(),
            List.of()
        ));
    }

    private void loadPlaceholderConflicts() {
        conflicts.put("WORK_DEADLINE", new ConflictDefView(
            "WORK_DEADLINE",
            "Рабочий дедлайн",
            "Начальник требует сделать работу в нереальные сроки",
            List.of(
                new ConflictDefView.TacticDefView(
                    "SURRENDER",
                    "Уступить",
                    "Согласиться и работать сверхурочно",
                    Map.of(),
                    -5,
                    Map.of("boss", 5),
                    Map.of(),
                    80,
                    Map.of()
                ),
                new ConflictDefView.TacticDefView(
                    "ASSERT",
                    "Настоять",
                    "Объяснить, что сроки нереальны",
                    Map.of("assertiveness", 30),
                    15,
                    Map.of("boss", -10),
                    Map.of("assertiveness", 2),
                    40,
                    Map.of("assertiveness", 20)
                ),
                new ConflictDefView.TacticDefView(
                    "COMPROMISE",
                    "Компромисс",
                    "Предложить реалистичный промежуточный вариант",
                    Map.of("communication", 20),
                    10,
                    Map.of("boss", 0),
                    Map.of("communication", 2),
                    60,
                    Map.of("communication", 15)
                )
            ),
            50 // baseStressPoints
        ));
    }

    private void loadPlaceholderQuests() {
        quests.put("CAREER_START", new QuestDefView(
            "CAREER_START",
            "Начало карьеры",
            "Освоиться на новой работе",
            "career",
            List.of(),
            Map.of(),
            1,
            List.of(
                new QuestDefView.QuestStepView(
                    "work_first_week",
                    "Отработать первую неделю",
                    "ACTION",
                    List.of("WORK"),
                    null,
                    null,
                    Map.of()
                ),
                new QuestDefView.QuestStepView(
                    "resolve_work_conflict",
                    "Разрешить первый рабочий конфликт",
                    "CONFLICT_RESOLUTION",
                    List.of(),
                    null,
                    "WORK_DEADLINE",
                    Map.of()
                )
            ),
            2000,
            Map.of("professional", 10),
            List.of("career_started"),
            "Ты успешно влилась в коллектив!"
        ));
    }

    private void loadPlaceholderEvents() {
        events.put("NEIGHBOR_COMPLAIN", new EventDefView(
            "NEIGHBOR_COMPLAIN",
            "Жалоба соседей",
            "Соседи снизу жалуются на шум от Дюка",
            "home",
            List.of(),
            Map.of(),
            7,
            null,
            List.of(
                new EventDefView.EventOptionView(
                    "APOLOGIZE",
                    "Извиниться и пообещать следить за щенком",
                    Map.of(),
                    List.of(),
                    Map.of("stress", 5, "selfEsteem", -5),
                    Map.of("neighbors", 10),
                    Map.of("empathy", 1),
                    0,
                    List.of(),
                    List.of(),
                    "Соседи приняли извинения, но будут следить."
                ),
                new EventDefView.EventOptionView(
                    "DEFEND",
                    "Защитить Дюка — он всего лишь щенок!",
                    Map.of("assertiveness", 20),
                    List.of(),
                    Map.of("stress", -5, "selfEsteem", 10),
                    Map.of("neighbors", -20),
                    Map.of("assertiveness", 2),
                    0,
                    List.of("neighbor_conflict"),
                    List.of(),
                    "Ты отстояла свою позицию, но соседи недовольны."
                )
            ),
            5,
            false
        ));
    }
}
