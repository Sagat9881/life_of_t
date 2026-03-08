package ru.lifegame.backend.application.engine;

import com.sagat.life_of_t.domain.engine.parser.EventSpecParser;
import com.sagat.life_of_t.domain.engine.parser.NpcSpecParser;
import com.sagat.life_of_t.domain.engine.parser.QuestSpecParser;
import com.sagat.life_of_t.domain.engine.spec.EventSpec;
import com.sagat.life_of_t.domain.engine.spec.NpcSpec;
import com.sagat.life_of_t.domain.engine.spec.QuestSpec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Scans narrative/ directory and loads all XML specs into registries.
 * This is the single point of contact between filesystem and domain.
 * The engine never references specific file names or NPC IDs.
 */
public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private final NpcRegistry npcRegistry = new NpcRegistry();
    private final List<EventSpec> eventSpecs = new ArrayList<>();
    private final List<QuestSpec> questSpecs = new ArrayList<>();

    public void loadFromClasspath(String basePath) {
        loadNpcs(basePath + "/npc-behavior");
        loadEvents(basePath + "/events");
        loadQuests(basePath + "/quests");
        initializeDefaultRelations();
    }

    public void loadFromFilesystem(Path basePath) {
        loadNpcsFs(basePath.resolve("npc-behavior"));
        loadEventsFs(basePath.resolve("events"));
        loadQuestsFs(basePath.resolve("quests"));
        initializeDefaultRelations();
    }

    private void loadNpcs(String classpathDir) {
        // In Spring context, this would scan classpath resources
        // For now, provide manual loading capability
    }

    private void loadNpcsFs(Path dir) {
        parseAllXmlInDir(dir, xml -> {
            NpcSpec spec = npcParser.parse(xml);
            npcRegistry.register(spec);
        });
    }

    private void loadEvents(String classpathDir) {}

    private void loadEventsFs(Path dir) {
        parseAllXmlInDir(dir, xml -> {
            EventSpec spec = eventParser.parse(xml);
            eventSpecs.add(spec);
        });
    }

    private void loadQuests(String classpathDir) {}

    private void loadQuestsFs(Path dir) {
        parseAllXmlInDir(dir, xml -> {
            QuestSpec spec = questParser.parse(xml);
            questSpecs.add(spec);
        });
    }

    private void parseAllXmlInDir(Path dir, XmlConsumer consumer) {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> files = Files.list(dir)) {
            files.filter(p -> p.toString().endsWith(".xml"))
                    .sorted()
                    .forEach(p -> {
                        try (InputStream is = Files.newInputStream(p)) {
                            consumer.accept(is);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse: " + p, e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan directory: " + dir, e);
        }
    }

    private void initializeDefaultRelations() {
        List<String> npcIds = npcRegistry.allNpcs().stream()
                .filter(n -> n.spec().isNamed())
                .map(n -> n.spec().entityId())
                .toList();

        for (int i = 0; i < npcIds.size(); i++) {
            for (int j = i + 1; j < npcIds.size(); j++) {
                npcRegistry.initializeRelation(npcIds.get(i), npcIds.get(j), 50, 0, 30);
            }
        }
    }

    @FunctionalInterface
    private interface XmlConsumer {
        void accept(InputStream xml) throws Exception;
    }

    public NpcRegistry npcRegistry() { return npcRegistry; }
    public List<EventSpec> eventSpecs() { return Collections.unmodifiableList(eventSpecs); }
    public List<QuestSpec> questSpecs() { return Collections.unmodifiableList(questSpecs); }
}
