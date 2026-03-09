package ru.lifegame.backend.application.service.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Loads conflict type definitions from narrative/conflicts XML.
 * Returns lightweight DTOs for frontend.
 */
@Service
public class ConflictContentService {
    private static final Logger log = LoggerFactory.getLogger(ConflictContentService.class);

    public List<Map<String, Object>> loadAllConflicts() {
        // TODO: parse from narrative/conflicts/*.xml
        return List.of(
            Map.of(
                "type", "WORK_PRESSURE",
                "name", "Work Pressure",
                "description", "Deadline stress is building up",
                "tactics", List.of(
                    Map.of(
                        "id", "confront_boss",
                        "name", "Confront Boss",
                        "effects", Map.of("stress", -10, "career", -5, "assertiveness", 5)
                    ),
                    Map.of(
                        "id", "work_overtime",
                        "name", "Work Overtime",
                        "effects", Map.of("stress", 15, "career", 10, "energy", -20)
                    ),
                    Map.of(
                        "id", "seek_support",
                        "name", "Seek Support",
                        "effects", Map.of("stress", -5, "relationships", 5)
                    )
                )
            ),
            Map.of(
                "type", "FAMILY_TENSION",
                "name", "Family Tension",
                "description", "Disagreement with Alexander",
                "tactics", List.of(
                    Map.of(
                        "id", "compromise",
                        "name", "Compromise",
                        "effects", Map.of("relationship_alexander", 5, "mood", 5)
                    ),
                    Map.of(
                        "id", "avoid",
                        "name", "Avoid",
                        "effects", Map.of("relationship_alexander", -3, "stress", 5)
                    ),
                    Map.of(
                        "id", "discuss",
                        "name", "Discuss Openly",
                        "effects", Map.of("relationship_alexander", 10, "communication", 5)
                    )
                )
            )
        );
    }
}
