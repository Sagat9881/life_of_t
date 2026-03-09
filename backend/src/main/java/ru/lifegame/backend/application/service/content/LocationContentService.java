package ru.lifegame.backend.application.service.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Loads location configs.
 * Returns lightweight DTOs for frontend.
 */
@Service
public class LocationContentService {
    private static final Logger log = LoggerFactory.getLogger(LocationContentService.class);

    public List<Map<String, Object>> loadAllLocations() {
        // TODO: load from config files or database
        return List.of(
            Map.of(
                "id", "living_room",
                "name", "Living Room",
                "assetName", "living_room",
                "objects", List.of(
                    Map.of(
                        "id", "sofa",
                        "name", "Sofa",
                        "x", 30,
                        "y", 60,
                        "actions", List.of("rest", "read")
                    ),
                    Map.of(
                        "id", "tv",
                        "name", "TV",
                        "x", 70,
                        "y", 40,
                        "actions", List.of("watch_tv")
                    )
                )
            ),
            Map.of(
                "id", "workplace",
                "name", "Office",
                "assetName", "office",
                "objects", List.of(
                    Map.of(
                        "id", "desk",
                        "name", "Desk",
                        "x", 50,
                        "y", 50,
                        "actions", List.of("work_office")
                    )
                )
            ),
            Map.of(
                "id", "dacha_yard",
                "name", "Dacha Yard",
                "assetName", "dacha_yard",
                "objects", List.of(
                    Map.of(
                        "id", "garden",
                        "name", "Garden",
                        "x", 40,
                        "y", 70,
                        "actions", List.of("garden_work")
                    ),
                    Map.of(
                        "id", "fence",
                        "name", "Fence",
                        "x", 80,
                        "y", 60,
                        "actions", List.of("walk_duke")
                    )
                )
            )
        );
    }
}
