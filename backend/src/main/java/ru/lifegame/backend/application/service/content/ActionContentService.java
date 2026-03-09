package ru.lifegame.backend.application.service.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Loads player action definitions from narrative/player-actions XML.
 * Returns lightweight DTOs for frontend.
 */
@Service
public class ActionContentService {
    private static final Logger log = LoggerFactory.getLogger(ActionContentService.class);

    public List<Map<String, Object>> loadAllActions() {
        // TODO: parse from narrative/player-actions/*.xml
        // For now return hardcoded examples
        return List.of(
            Map.of(
                "code", "work_office",
                "name", "Work at Office",
                "location", "workplace",
                "duration", 240, // minutes
                "energyCost", -15,
                "effects", Map.of(
                    "money", 500,
                    "stress", 10,
                    "careerProgress", 5
                ),
                "requirements", Map.of(
                    "minEnergy", 20,
                    "timeOfDay", List.of("morning", "day")
                ),
                "animation", "work"
            ),
            Map.of(
                "code", "walk_duke",
                "name", "Walk Duke",
                "location", "dacha_yard",
                "duration", 30,
                "energyCost", -5,
                "effects", Map.of(
                    "mood", 10,
                    "duke_mood", 15,
                    "relationship_duke", 2
                ),
                "requirements", Map.of(
                    "minEnergy", 10,
                    "npc_present", "duke"
                ),
                "animation", "walk"
            ),
            Map.of(
                "code", "rest",
                "name", "Rest",
                "location", "any",
                "duration", 60,
                "energyCost", 0,
                "effects", Map.of(
                    "energy", 20,
                    "stress", -5
                ),
                "requirements", Map.of(),
                "animation", "idle"
            )
        );
    }
}
