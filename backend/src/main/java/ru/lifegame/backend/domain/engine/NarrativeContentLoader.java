package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    public void loadFromDirectory(String basePath) {
        npcSpecs = loadSpecs(basePath + "/npc-behavior", npcParser::parse);
        eventSpecs = loadSpecs(basePath + "/events", eventParser::parse);
        questSpecs = loadSpecs(basePath + "/quests", questParser::parse);
    }

    private <T> List<T> loadSpecs(String dirPath, SpecParser<T> parser) {
        List<T> results = new ArrayList<>();
        Path dir = Paths.get(dirPath);
        if (!Files.exists(dir)) return results;
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".xml"))
                 .forEach(p -> {
                     try {
                         results.add(parser.parse(p));
                     } catch (Exception e) {
                         System.err.println("Failed to parse: " + p + " - " + e.getMessage());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Failed to scan directory: " + dirPath + " - " + e.getMessage());
        }
        return results;
    }

    @FunctionalInterface
    interface SpecParser<T> {
        T parse(Path path) throws Exception;
    }

    public List<NpcSpec> npcSpecs() { return Collections.unmodifiableList(npcSpecs); }
    public List<EventSpec> eventSpecs() { return Collections.unmodifiableList(eventSpecs); }
    public List<QuestSpec> questSpecs() { return Collections.unmodifiableList(questSpecs); }
}
