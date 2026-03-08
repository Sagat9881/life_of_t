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

    private final NpcSpecParser npcParser;
    private final QuestSpecParser questParser;
    private final EventSpecParser eventParser;

    private List<NpcSpec> npcSpecs = List.of();
    private List<QuestSpec> questSpecs = List.of();
    private List<EventSpec> eventSpecs = List.of();

    public NarrativeContentLoader(NpcSpecParser npcParser, QuestSpecParser questParser, EventSpecParser eventParser) {
        this.npcParser = npcParser;
        this.questParser = questParser;
        this.eventParser = eventParser;
    }

    public void loadFromClasspath(String basePath) {
        npcSpecs = loadSpecs(basePath + "/npc-behavior", npcParser::parse);
        questSpecs = loadSpecs(basePath + "/quests", questParser::parse);
        eventSpecs = loadSpecs(basePath + "/events", eventParser::parse);
    }

    @FunctionalInterface
    interface XmlParser<T> {
        T parse(InputStream is) throws Exception;
    }

    private <T> List<T> loadSpecs(String dir, XmlParser<T> parser) {
        List<T> results = new ArrayList<>();
        try {
            Path dirPath = Paths.get(ClassLoader.getSystemResource(dir).toURI());
            try (Stream<Path> files = Files.list(dirPath)) {
                files.filter(p -> p.toString().endsWith(".xml")).forEach(p -> {
                    try (InputStream is = Files.newInputStream(p)) {
                        results.add(parser.parse(is));
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse: " + p, e);
                    }
                });
            }
        } catch (Exception e) {
            // Directory not found - no specs of this type
        }
        return results;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
