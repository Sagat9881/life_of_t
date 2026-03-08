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

    public void loadFromClasspath(String basePath) {
        loadNpcs(basePath + "/npc-behavior");
        loadEvents(basePath + "/events");
        loadQuests(basePath + "/quests");
    }

    private void loadNpcs(String path) {
        loadXmlFiles(path, xml -> npcSpecs.add(npcParser.parse(xml)));
    }

    private void loadEvents(String path) {
        loadXmlFiles(path, xml -> eventSpecs.add(eventParser.parse(xml)));
    }

    private void loadQuests(String path) {
        loadXmlFiles(path, xml -> questSpecs.add(questParser.parse(xml)));
    }

    private void loadXmlFiles(String path, XmlConsumer consumer) {
        try {
            var classLoader = getClass().getClassLoader();
            var resource = classLoader.getResource(path);
            if (resource == null) return;
            var uri = resource.toURI();
            Path dir = Paths.get(uri);
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(f -> f.toString().endsWith(".xml")).forEach(f -> {
                    try (InputStream is = Files.newInputStream(f)) {
                        consumer.accept(is);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse: " + f, e);
                    }
                });
            }
        } catch (Exception e) {
            // Directory not found — skip silently
        }
    }

    @FunctionalInterface
    private interface XmlConsumer {
        void accept(InputStream xml) throws Exception;
    }

    public List<NpcSpec> npcSpecs() { return Collections.unmodifiableList(npcSpecs); }
    public List<EventSpec> eventSpecs() { return Collections.unmodifiableList(eventSpecs); }
    public List<QuestSpec> questSpecs() { return Collections.unmodifiableList(questSpecs); }
}
