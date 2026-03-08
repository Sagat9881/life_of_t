package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser;
    private final QuestSpecParser questParser;
    private final EventSpecParser eventParser;

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();

    public NarrativeContentLoader(NpcSpecParser npcParser, QuestSpecParser questParser, EventSpecParser eventParser) {
        this.npcParser = npcParser;
        this.questParser = questParser;
        this.eventParser = eventParser;
    }

    public void loadAll(String narrativeBasePath) throws IOException {
        Path base = Paths.get(narrativeBasePath);
        npcSpecs = loadFromDirectory(base.resolve("npc-behavior"), npcParser::parse);
        questSpecs = loadFromDirectory(base.resolve("quests"), questParser::parse);
        eventSpecs = loadFromDirectory(base.resolve("events"), eventParser::parse);
    }

    private <T> List<T> loadFromDirectory(Path dir, XmlFileParser<T> parser) throws IOException {
        if (!Files.exists(dir)) return List.of();
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(p -> p.toString().endsWith(".xml"))
                    .map(p -> {
                        try { return parser.parse(p.toFile()); }
                        catch (Exception e) { throw new RuntimeException("Failed to parse: " + p, e); }
                    })
                    .collect(Collectors.toList());
        }
    }

    @FunctionalInterface
    interface XmlFileParser<T> {
        T parse(File file) throws Exception;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
