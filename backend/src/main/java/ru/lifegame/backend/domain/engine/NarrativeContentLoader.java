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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();

    public void loadFromDirectory(String basePath) {
        npcSpecs = loadSpecs(basePath + "/npc-behavior", npcParser::parse);
        questSpecs = loadSpecs(basePath + "/quests", questParser::parse);
        eventSpecs = loadSpecs(basePath + "/events", eventParser::parse);
    }

    private <T> List<T> loadSpecs(String dirPath, java.util.function.Function<InputStream, T> parser) {
        List<T> results = new ArrayList<>();
        try {
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) return results;
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(f -> f.toString().endsWith(".xml"))
                     .forEach(f -> {
                         try (InputStream is = Files.newInputStream(f)) {
                             results.add(parser.apply(is));
                         } catch (Exception e) {
                             System.err.println("Failed to parse: " + f + " - " + e.getMessage());
                         }
                     });
            }
        } catch (Exception e) {
            System.err.println("Failed to scan directory: " + dirPath + " - " + e.getMessage());
        }
        return results;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
