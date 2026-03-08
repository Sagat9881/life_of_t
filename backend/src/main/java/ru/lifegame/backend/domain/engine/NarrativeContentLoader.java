package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final List<NpcSpec> npcSpecs = new ArrayList<>();
    private final List<EventSpec> eventSpecs = new ArrayList<>();
    private final List<QuestSpec> questSpecs = new ArrayList<>();

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    public void loadFromClasspath(String basePath) {
        loadNpcSpecs(basePath + "/npc-behavior");
        loadEventSpecs(basePath + "/events");
        loadQuestSpecs(basePath + "/quests");
    }

    private void loadNpcSpecs(String dir) {
        // Load NPC XML specs from classpath directory
    }

    private void loadEventSpecs(String dir) {
        // Load event XML specs from classpath directory
    }

    private void loadQuestSpecs(String dir) {
        // Load quest XML specs from classpath directory
    }

    public List<NpcSpec> npcSpecs() { return Collections.unmodifiableList(npcSpecs); }
    public List<EventSpec> eventSpecs() { return Collections.unmodifiableList(eventSpecs); }
    public List<QuestSpec> questSpecs() { return Collections.unmodifiableList(questSpecs); }
}
