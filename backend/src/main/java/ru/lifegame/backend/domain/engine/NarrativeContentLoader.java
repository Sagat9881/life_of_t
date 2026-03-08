package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public void loadAll(String narrativeBasePath) {
        loadNpcs(narrativeBasePath + "/npc-behavior");
        loadEvents(narrativeBasePath + "/events");
        loadQuests(narrativeBasePath + "/quests");
    }

    private void loadNpcs(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            npcSpecs.add(npcParser.parse(f));
        }
    }

    private void loadEvents(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            eventSpecs.add(eventParser.parse(f));
        }
    }

    private void loadQuests(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            questSpecs.add(questParser.parse(f));
        }
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
