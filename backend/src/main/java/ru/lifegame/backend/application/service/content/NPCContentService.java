package ru.lifegame.backend.application.service.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Loads NPC definitions from narrative/npc-behavior XML.
 * Returns lightweight DTOs for frontend.
 */
@Service
public class NPCContentService {
    private static final Logger log = LoggerFactory.getLogger(NPCContentService.class);

    public List<Map<String, Object>> loadAllNPCs() {
        // TODO: parse from narrative/npc-behavior/*.xml
        return List.of(
            Map.of(
                "id", "alexander",
                "name", "Alexander",
                "type", "family",
                "personality", Map.of(
                    "openness", 0.7,
                    "conscientiousness", 0.8,
                    "extraversion", 0.4,
                    "agreeableness", 0.6,
                    "neuroticism", 0.5
                ),
                "schedule", List.of(
                    Map.of("time", "06:00-09:00", "activity", "SLEEPING", "location", "bedroom"),
                    Map.of("time", "09:00-18:00", "activity", "WORKING", "location", "workplace"),
                    Map.of("time", "18:00-22:00", "activity", "RELAXING", "location", "living_room"),
                    Map.of("time", "22:00-06:00", "activity", "SLEEPING", "location", "bedroom")
                ),
                "relationships", Map.of(
                    "persi", 85,
                    "duke", 70
                )
            ),
            Map.of(
                "id", "duke",
                "name", "Duke",
                "type", "pet",
                "personality", Map.of(
                    "energy", 0.9,
                    "loyalty", 1.0,
                    "independence", 0.2
                ),
                "schedule", List.of(
                    Map.of("time", "00:00-08:00", "activity", "SLEEPING", "location", "bedroom"),
                    Map.of("time", "08:00-12:00", "activity", "PLAYING", "location", "living_room"),
                    Map.of("time", "12:00-14:00", "activity", "SLEEPING", "location", "living_room"),
                    Map.of("time", "14:00-18:00", "activity", "FOLLOWING_PERSI", "location", "follow"),
                    Map.of("time", "18:00-22:00", "activity", "PLAYING", "location", "living_room"),
                    Map.of("time", "22:00-00:00", "activity", "SLEEPING", "location", "bedroom")
                ),
                "relationships", Map.of(
                    "persi", 100,
                    "alexander", 70
                )
            )
        );
    }
}
