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

import javax.xml.parsers.DocumentBuilder;
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

    public void loadAll(String basePath) {
        npcSpecs = loadDirectory(basePath + "/npc-behavior", npcParser::parse);
        eventSpecs = loadDirectory(basePath + "/events", eventParser::parse);
        questSpecs = loadDirectory(basePath + "/quests", questParser::parse);
    }

    private <T> List<T> loadDirectory(String path, java.util.function.Function<Document, T> parser) {
        List<T> results = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:" + path + "/*.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = builder.parse(is);
                    doc.getDocumentElement().normalize();
                    results.add(parser.apply(doc));
                }
            }
        } catch (Exception e) {
            // Log warning but don't fail - missing directories are ok for MVP
        }
        return results;
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
