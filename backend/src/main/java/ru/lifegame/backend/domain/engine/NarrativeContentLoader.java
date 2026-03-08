package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    public void loadFromDirectory(Path narrativeRoot) throws IOException {
        Path npcDir = narrativeRoot.resolve("npc-behavior");
        Path eventDir = narrativeRoot.resolve("events");
        Path questDir = narrativeRoot.resolve("quests");

        if (Files.isDirectory(npcDir)) {
            try (Stream<Path> files = Files.list(npcDir)) {
                files.filter(p -> p.toString().endsWith(".xml"))
                     .forEach(p -> {
                         try { npcSpecs.add(npcParser.parse(p)); }
                         catch (Exception e) { System.err.println("Failed to parse NPC: " + p + " - " + e.getMessage()); }
                     });
            }
        }

        if (Files.isDirectory(eventDir)) {
            try (Stream<Path> files = Files.list(eventDir)) {
                files.filter(p -> p.toString().endsWith(".xml"))
                     .forEach(p -> {
                         try { eventSpecs.add(eventParser.parse(p)); }
                         catch (Exception e) { System.err.println("Failed to parse event: " + p + " - " + e.getMessage()); }
                     });
            }
        }

        if (Files.isDirectory(questDir)) {
            try (Stream<Path> files = Files.list(questDir)) {
                files.filter(p -> p.toString().endsWith(".xml"))
                     .forEach(p -> {
                         try { questSpecs.add(questParser.parse(p)); }
                         catch (Exception e) { System.err.println("Failed to parse quest: " + p + " - " + e.getMessage()); }
                     });
            }
        }
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
