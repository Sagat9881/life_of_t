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

    public void loadFromDirectory(String basePath) {
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
            try {
                npcSpecs.add(npcParser.parse(file));
            } catch (Exception e) {
                System.err.println("Failed to parse NPC spec: " + file.getName() + " - " + e.getMessage());
            }
        }
    }

    private void loadQuests(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File file : files) {
            try {
                questSpecs.add(questParser.parse(file));
            } catch (Exception e) {
                System.err.println("Failed to parse quest spec: " + file.getName() + " - " + e.getMessage());
            }
        }
    }

    private void loadEvents(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (files == null) return;
        for (File file : files) {
            try {
                eventSpecs.add(eventParser.parse(file));
            } catch (Exception e) {
                System.err.println("Failed to parse event spec: " + file.getName() + " - " + e.getMessage());
            }
        }
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
