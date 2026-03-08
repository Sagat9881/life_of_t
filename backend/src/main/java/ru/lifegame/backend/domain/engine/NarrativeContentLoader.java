package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();

    public void loadAll() {
        npcSpecs = loadFromPattern("classpath*:narrative/npc-behavior/*.xml", npcParser::parse);
        questSpecs = loadFromPattern("classpath*:narrative/quests/*.xml", questParser::parse);
        eventSpecs = loadFromPattern("classpath*:narrative/events/*.xml", eventParser::parse);
    }

    private <T> List<T> loadFromPattern(String pattern, java.util.function.Function<Document, T> parser) {
        List<T> results = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pattern);
            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                    results.add(parser.apply(doc));
                } catch (Exception e) {
                    System.err.println("Failed to parse: " + resource.getFilename() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to scan pattern: " + pattern + " - " + e.getMessage());
        }
        return results;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
}
