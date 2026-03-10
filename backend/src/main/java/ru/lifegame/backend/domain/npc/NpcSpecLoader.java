package ru.lifegame.backend.domain.npc;

import ru.lifegame.backend.domain.narrative.parser.NpcSpecParser;
import ru.lifegame.backend.domain.npc.spec.NpcSpec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads NPC specifications from XML files in narrative/npc-behavior/.
 *
 * Uses {@link NpcSpecParser} for all DOM parsing.
 * Resources are read via {@link Resource#getInputStream()} so loading works
 * both on the filesystem and inside a JAR.
 */
public class NpcSpecLoader {

    private static final String NPC_SPEC_PATTERN = "classpath:narrative/npc-behavior/*.xml";

    private final NpcSpecParser parser = new NpcSpecParser();

    public List<NpcSpec> loadAll() {
        List<NpcSpec> specs = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(NPC_SPEC_PATTERN);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                try (InputStream is = resource.getInputStream()) {
                    specs.add(parser.parse(is, filename));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse NPC spec: " + filename, e);
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan NPC spec pattern: " + NPC_SPEC_PATTERN, e);
        }
        return specs;
    }
}
