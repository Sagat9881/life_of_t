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

    public void loadAll(String basePath) {
        loadNpcs(basePath + "/npc-behavior");
        loadQuests(basePath + "/quests");
        loadEvents(basePath + "/events");
    }

    private void loadNpcs(String path) {
        File dir = new File(path);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        npcSpecs = Arrays.stream(files)
                .map(f -> npcParser.parse(f.getAbsolutePath()))
                .collect(Collectors.toList());
    }

    private void loadQuests(String path) {
        File dir = new File(path);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        questSpecs = Arrays.stream(files)
                .map(f -> questParser.parse(f.getAbsolutePath()))
                .collect(Collectors.toList());
    }

    private void loadEvents(String path) {
        File dir = new File(path);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        eventSpecs = Arrays.stream(files)
                .map(f -> eventParser.parse(f.getAbsolutePath()))
                .collect(Collectors.toList());
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
