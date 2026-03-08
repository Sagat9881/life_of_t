package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser;
    private final EventSpecParser eventParser;
    private final QuestSpecParser questParser;

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    public NarrativeContentLoader(NpcSpecParser npcParser, EventSpecParser eventParser, QuestSpecParser questParser) {
        this.npcParser = npcParser;
        this.eventParser = eventParser;
        this.questParser = questParser;
    }

    public void loadFromClasspath(String basePath) {
        npcSpecs = loadDirectory(basePath + "/npc-behavior", npcParser::parse);
        eventSpecs = loadDirectory(basePath + "/events", eventParser::parse);
        questSpecs = loadDirectory(basePath + "/quests", questParser::parse);
    }

    private <T> List<T> loadDirectory(String dirPath, java.util.function.Function<InputStream, T> parser) {
        List<T> results = new ArrayList<>();
        try {
            Path dir = Paths.get(ClassLoader.getSystemResource(dirPath).toURI());
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
            System.err.println("Directory not found: " + dirPath + " - " + e.getMessage());
        }
        return results;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
