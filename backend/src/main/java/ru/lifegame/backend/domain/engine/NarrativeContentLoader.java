package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser;
    private final QuestSpecParser questParser;
    private final EventSpecParser eventParser;

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();

    public NarrativeContentLoader(NpcSpecParser npcParser, QuestSpecParser questParser,
                                   EventSpecParser eventParser) {
        this.npcParser = npcParser;
        this.questParser = questParser;
        this.eventParser = eventParser;
    }

    public void loadAll(String basePath) {
        loadNpcs(basePath + "/npc-behavior");
        loadQuests(basePath + "/quests");
        loadEvents(basePath + "/events");
    }

    private void loadNpcs(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File file : files) {
            npcSpecs.add(npcParser.parse(file));
        }
    }

    private void loadQuests(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File file : files) {
            questSpecs.add(questParser.parse(file));
        }
    }

    private void loadEvents(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File file : files) {
            eventSpecs.add(eventParser.parse(file));
        }
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
