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

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    public void loadFromDirectory(String basePath) {
        loadNpcs(new File(basePath, "npc-behavior"));
        loadEvents(new File(basePath, "events"));
        loadQuests(new File(basePath, "quests"));
    }

    private void loadNpcs(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            if (files != null) {
                for (File f : files) {
                    try { npcSpecs.add(npcParser.parse(f)); }
                    catch (Exception e) { System.err.println("Failed to parse NPC spec: " + f.getName() + " - " + e.getMessage()); }
                }
            }
        }
    }

    private void loadEvents(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            if (files != null) {
                for (File f : files) {
                    try { eventSpecs.add(eventParser.parse(f)); }
                    catch (Exception e) { System.err.println("Failed to parse event spec: " + f.getName() + " - " + e.getMessage()); }
                }
            }
        }
    }

    private void loadQuests(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            if (files != null) {
                for (File f : files) {
                    try { questSpecs.add(questParser.parse(f)); }
                    catch (Exception e) { System.err.println("Failed to parse quest spec: " + f.getName() + " - " + e.getMessage()); }
                }
            }
        }
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
