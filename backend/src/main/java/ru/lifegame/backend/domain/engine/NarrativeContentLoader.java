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

    private List<NpcSpec> npcSpecs = List.of();
    private List<EventSpec> eventSpecs = List.of();
    private List<QuestSpec> questSpecs = List.of();

    public void loadFromDirectory(String basePath) {
        npcSpecs = loadXmlFiles(basePath + "/npc-behavior", npcParser::parse);
        eventSpecs = loadXmlFiles(basePath + "/events", eventParser::parse);
        questSpecs = loadXmlFiles(basePath + "/quests", questParser::parse);
    }

    private <T> List<T> loadXmlFiles(String dirPath, java.util.function.Function<InputStream, T> parser) {
        Path dir = Paths.get(dirPath);
        if (!Files.isDirectory(dir)) return List.of();
        try (Stream<Path> paths = Files.list(dir)) {
            return paths
                    .filter(p -> p.toString().endsWith(".xml"))
                    .map(p -> {
                        try { return parser.apply(Files.newInputStream(p)); }
                        catch (IOException e) { throw new UncheckedIOException(e); }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
