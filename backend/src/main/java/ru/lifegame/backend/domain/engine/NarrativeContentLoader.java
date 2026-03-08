package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class NarrativeContentLoader {

    private final String narrativeBasePath;
    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();

    private List<NpcSpec> npcSpecs = List.of();
    private List<QuestSpec> questSpecs = List.of();
    private List<EventSpec> eventSpecs = List.of();

    public NarrativeContentLoader(String narrativeBasePath) {
        this.narrativeBasePath = narrativeBasePath;
    }

    public void loadAll() {
        npcSpecs = loadDirectory(narrativeBasePath + "/npc-behavior", npcParser::parse);
        questSpecs = loadDirectory(narrativeBasePath + "/quests", questParser::parse);
        eventSpecs = loadDirectory(narrativeBasePath + "/events", eventParser::parse);
    }

    private <T> List<T> loadDirectory(String dirPath, java.util.function.Function<File, T> parser) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) return List.of();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return List.of();
        return Arrays.stream(files)
                .map(parser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
