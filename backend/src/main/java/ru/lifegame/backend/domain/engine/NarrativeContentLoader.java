package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();

    private List<NpcSpec> npcSpecs = List.of();
    private List<QuestSpec> questSpecs = List.of();
    private List<EventSpec> eventSpecs = List.of();

    public void loadFromDirectory(String basePath) {
        npcSpecs = loadSpecs(basePath + "/npc-behavior", npcParser::parse);
        questSpecs = loadSpecs(basePath + "/quests", questParser::parse);
        eventSpecs = loadSpecs(basePath + "/events", eventParser::parse);
    }

    private <T> List<T> loadSpecs(String dirPath, java.util.function.Function<InputStream, T> parser) {
        List<T> result = new ArrayList<>();
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) return result;
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(f -> f.toString().endsWith(".xml"))
                     .forEach(f -> {
                         try (InputStream is = Files.newInputStream(f)) {
                             T spec = parser.apply(is);
                             if (spec != null) result.add(spec);
                         } catch (Exception e) {
                             System.err.println("Failed to parse: " + f + " - " + e.getMessage());
                         }
                     });
            }
        } catch (Exception e) {
            System.err.println("Failed to load specs from: " + dirPath + " - " + e.getMessage());
        }
        return result;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
