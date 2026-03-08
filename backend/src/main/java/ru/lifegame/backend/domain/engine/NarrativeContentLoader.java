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

    private <T> List<T> loadSpecs(String dir, java.util.function.Function<InputStream, T> parser) {
        List<T> results = new ArrayList<>();
        try {
            var resource = getClass().getClassLoader().getResource(dir);
            if (resource == null) return results;
            Path path = Paths.get(resource.toURI());
            try (Stream<Path> files = Files.list(path)) {
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
            System.err.println("Failed to load specs from: " + dir + " - " + e.getMessage());
        }
        return results;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
