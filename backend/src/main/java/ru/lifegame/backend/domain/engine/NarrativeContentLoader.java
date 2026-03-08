package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    public void loadFromClasspath(String basePath) {
        npcSpecs = loadAndParse(basePath + "/npc-behavior", npcParser::parse);
        eventSpecs = loadAndParse(basePath + "/events", eventParser::parse);
        questSpecs = loadAndParse(basePath + "/quests", questParser::parse);
    }

    private <T> List<T> loadAndParse(String dirPath, java.util.function.Function<Document, T> parser) {
        List<T> results = new ArrayList<>();
        try {
            Path dir = Paths.get(ClassLoader.getSystemResource(dirPath).toURI());
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(f -> f.toString().endsWith(".xml")).forEach(file -> {
                    try (InputStream is = Files.newInputStream(file)) {
                        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document doc = db.parse(is);
                        results.add(parser.apply(doc));
                    } catch (Exception e) {
                        System.err.println("Failed to parse: " + file + " - " + e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to load directory: " + dirPath + " - " + e.getMessage());
        }
        return results;
    }

    public List<NpcSpec> getNpcSpecs() { return npcSpecs; }
    public List<EventSpec> getEventSpecs() { return eventSpecs; }
    public List<QuestSpec> getQuestSpecs() { return questSpecs; }
}
