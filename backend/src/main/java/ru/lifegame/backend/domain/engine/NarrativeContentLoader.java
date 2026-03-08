package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.NpcSpecParser;
import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.NpcSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NarrativeContentLoader {

    private final NpcSpecParser npcParser = new NpcSpecParser();
    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private List<NpcSpec> npcSpecs = new ArrayList<>();
    private List<EventSpec> eventSpecs = new ArrayList<>();
    private List<QuestSpec> questSpecs = new ArrayList<>();

    public void loadAll() {
        npcSpecs = loadFromPattern("classpath*:narrative/npc-behavior/*.xml", npcParser::parse);
        eventSpecs = loadFromPattern("classpath*:narrative/events/*.xml", eventParser::parse);
        questSpecs = loadFromPattern("classpath*:narrative/quests/*.xml", questParser::parse);
    }

    private <T> List<T> loadFromPattern(String pattern, java.util.function.Function<Document, T> parser) {
        List<T> results = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(pattern);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = factory.newDocumentBuilder().parse(is);
                    results.add(parser.apply(doc));
                }
            }
        } catch (Exception e) {
            // Log warning but don't fail startup
        }
        return results;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
