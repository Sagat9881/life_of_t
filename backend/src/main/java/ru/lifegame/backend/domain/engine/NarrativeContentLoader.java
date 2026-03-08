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

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private final List<NpcSpec> npcSpecs = new ArrayList<>();
    private final List<EventSpec> eventSpecs = new ArrayList<>();
    private final List<QuestSpec> questSpecs = new ArrayList<>();

    public void loadFromDirectory(Path narrativeRoot) throws Exception {
        loadNpcs(narrativeRoot.resolve("npc-behavior"));
        loadEvents(narrativeRoot.resolve("events"));
        loadQuests(narrativeRoot.resolve("quests"));
    }

    private void loadNpcs(Path dir) throws Exception {
        if (!Files.exists(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".xml")).toList()) {
                try (InputStream is = Files.newInputStream(file)) {
                    npcSpecs.add(npcParser.parse(is));
                }
            }
        }
    }

    private void loadEvents(Path dir) throws Exception {
        if (!Files.exists(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".xml")).toList()) {
                try (InputStream is = Files.newInputStream(file)) {
                    eventSpecs.add(eventParser.parse(is));
                }
            }
        }
    }

    private void loadQuests(Path dir) throws Exception {
        if (!Files.exists(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".xml")).toList()) {
                try (InputStream is = Files.newInputStream(file)) {
                    questSpecs.add(questParser.parse(is));
                }
            }
        }
    }

    public List<NpcSpec> npcSpecs() { return Collections.unmodifiableList(npcSpecs); }
    public List<EventSpec> eventSpecs() { return Collections.unmodifiableList(eventSpecs); }
    public List<QuestSpec> questSpecs() { return Collections.unmodifiableList(questSpecs); }
}
