package ru.lifegame.backend.domain.engine;

import ru.lifegame.backend.domain.engine.parser.EventSpecParser;
import ru.lifegame.backend.domain.engine.parser.QuestSpecParser;
import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NarrativeContentLoader {

    private static final String EVENTS_PATTERN = "classpath:narrative/events/*.xml";
    private static final String QUESTS_PATTERN = "classpath:narrative/quests/*.xml";

    private final EventSpecParser eventParser = new EventSpecParser();
    private final QuestSpecParser questParser = new QuestSpecParser();

    private final List<EventSpec> eventSpecs = new ArrayList<>();
    private final List<QuestSpec> questSpecs = new ArrayList<>();

    /**
     * Load events and quests from classpath resources.
     * Called by NarrativeBootstrap on ApplicationReadyEvent.
     */
    public void loadFromClasspath() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        loadEvents(resolver);
        loadQuests(resolver);
    }

    /**
     * Legacy method: load from filesystem directory.
     */
    public void loadFromDirectory(String basePath) {
        loadEventsFromDir(new File(basePath, "events"));
        loadQuestsFromDir(new File(basePath, "quests"));
    }

    private void loadEvents(PathMatchingResourcePatternResolver resolver) {
        try {
            Resource[] resources = resolver.getResources(EVENTS_PATTERN);
            for (Resource resource : resources) {
                try {
                    File tempFile = copyToTemp(resource, "event");
                    eventSpecs.add(eventParser.parse(tempFile));
                    tempFile.delete();
                } catch (Exception e) {
                    System.err.println("Failed to parse event spec: "
                            + resource.getFilename() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("No event specs found on classpath: " + e.getMessage());
        }
    }

    private void loadQuests(PathMatchingResourcePatternResolver resolver) {
        try {
            Resource[] resources = resolver.getResources(QUESTS_PATTERN);
            for (Resource resource : resources) {
                try {
                    File tempFile = copyToTemp(resource, "quest");
                    questSpecs.add(questParser.parse(tempFile));
                    tempFile.delete();
                } catch (Exception e) {
                    System.err.println("Failed to parse quest spec: "
                            + resource.getFilename() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("No quest specs found on classpath: " + e.getMessage());
        }
    }

    private File copyToTemp(Resource resource, String prefix) throws Exception {
        Path temp = Files.createTempFile(prefix + "-", ".xml");
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return temp.toFile();
    }

    private void loadEventsFromDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            if (files != null) {
                for (File f : files) {
                    try { eventSpecs.add(eventParser.parse(f)); }
                    catch (Exception e) { System.err.println("Failed to parse event spec: " + f.getName() + " - " + e.getMessage()); }
                }
            }
        }
    }

    private void loadQuestsFromDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".xml"));
            if (files != null) {
                for (File f : files) {
                    try { questSpecs.add(questParser.parse(f)); }
                    catch (Exception e) { System.err.println("Failed to parse quest spec: " + f.getName() + " - " + e.getMessage()); }
                }
            }
        }
    }

    public List<EventSpec> eventSpecs() { return eventSpecs; }
    public List<QuestSpec> questSpecs() { return questSpecs; }
}
