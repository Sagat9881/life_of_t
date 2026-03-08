package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import java.io.File;
import java.util.*;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser;
    private final EventSpecParser eventParser;
    private final QuestSpecParser questParser;

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    public NarrativeContentLoader(
        NpcSpecParser npcParser,
        EventSpecParser eventParser,
        QuestSpecParser questParser
    ) {
        this.npcParser = npcParser;
        this.eventParser = eventParser;
        this.questParser = questParser;
    }

    public void loadAll(String basePath) {
        loadNpcs(basePath + "/npc-behavior");
        loadEvents(basePath + "/events");
        loadQuests(basePath + "/quests");
    }

    private void loadNpcs(String dir) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            npcParser.parse(f.getAbsolutePath()).ifPresent(npcSpecs::add);
        }
    }

    private void loadEvents(String dir) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            eventParser.parse(f.getAbsolutePath()).ifPresent(eventSpecs::add);
        }
    }

    private void loadQuests(String dir) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            questParser.parse(f.getAbsolutePath()).ifPresent(questSpecs::add);
        }
    }

    public List<NpcSpec> npcSpecs() { return Collections.unmodifiableList(npcSpecs); }
    public List<EventSpec> eventSpecs() { return Collections.unmodifiableList(eventSpecs); }
    public List<QuestSpec> questSpecs() { return Collections.unmodifiableList(questSpecs); }
}
