package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class NarrativeContentLoader {

    @Value("${narrative.base-path:narrative}")
    private String basePath;

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    @PostConstruct
    public void load() {
        npcSpecs = loadFromDirectory(basePath + "/npc-behavior", npcParser::parse);
        eventSpecs = loadFromDirectory(basePath + "/events", eventParser::parse);
        questSpecs = loadFromDirectory(basePath + "/quests", questParser::parse);
    }

    private <T> List<T> loadFromDirectory(String dirPath, java.util.function.Function<File, T> parser) {
        File dir = new File(dirPath);
        if (!dir.exists() || !dir.isDirectory()) return List.of();
        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (xmlFiles == null) return List.of();
        return Arrays.stream(xmlFiles)
            .map(parser)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public List<NpcSpec> getNpcSpecs() { return npcSpecs; }
    public List<EventSpec> getEventSpecs() { return eventSpecs; }
    public List<QuestSpec> getQuestSpecs() { return questSpecs; }
}
