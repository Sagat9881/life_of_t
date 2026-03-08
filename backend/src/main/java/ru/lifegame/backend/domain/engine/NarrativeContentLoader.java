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

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();

    public void loadAll(String basePath) {
        loadNpcs(basePath + "/npc-behavior");
        loadQuests(basePath + "/quests");
        loadEvents(basePath + "/events");
    }

    private void loadNpcs(String dir) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            try { npcSpecs.add(npcParser.parse(f)); }
            catch (Exception e) { System.err.println("Failed to parse NPC: " + f.getName() + " - " + e.getMessage()); }
        }
    }

    private void loadQuests(String dir) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            try { questSpecs.add(questParser.parse(f)); }
            catch (Exception e) { System.err.println("Failed to parse Quest: " + f.getName() + " - " + e.getMessage()); }
        }
    }

    private void loadEvents(String dir) {
        File[] files = new File(dir).listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File f : files) {
            try { eventSpecs.add(eventParser.parse(f)); }
            catch (Exception e) { System.err.println("Failed to parse Event: " + f.getName() + " - " + e.getMessage()); }
        }
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
