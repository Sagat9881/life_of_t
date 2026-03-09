package ru.lifegame.backend.application.service.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Loads quest definitions from narrative/quests XML.
 * Returns lightweight DTOs for frontend.
 */
@Service
public class QuestContentService {
    private static final Logger log = LoggerFactory.getLogger(QuestContentService.class);

    public List<Map<String, Object>> loadAllQuests() {
        // TODO: parse from narrative/quests/*.xml
        return List.of(
            Map.of(
                "id", "career_breakthrough",
                "name", "Career Breakthrough",
                "description", "Get promoted at work",
                "steps", List.of(
                    Map.of(
                        "id", "prove_worth",
                        "description", "Complete 10 successful work sessions",
                        "requirement", Map.of("work_sessions", 10)
                    ),
                    Map.of(
                        "id", "impress_boss",
                        "description", "Achieve high career score",
                        "requirement", Map.of("career_score", 80)
                    )
                ),
                "rewards", Map.of(
                    "money", 5000,
                    "career", 20,
                    "mood", 15
                )
            ),
            Map.of(
                "id", "duke_training",
                "name", "Duke's Training",
                "description", "Train Duke to be a good boy",
                "steps", List.of(
                    Map.of(
                        "id", "basic_commands",
                        "description", "Teach Duke basic commands",
                        "requirement", Map.of("training_sessions", 5)
                    ),
                    Map.of(
                        "id", "bond",
                        "description", "Build strong bond with Duke",
                        "requirement", Map.of("relationship_duke", 90)
                    )
                ),
                "rewards", Map.of(
                    "relationship_duke", 10,
                    "mood", 10
                )
            )
        );
    }
}
