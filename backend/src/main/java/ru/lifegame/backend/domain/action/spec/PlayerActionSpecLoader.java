package ru.lifegame.backend.domain.action.spec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Loads PlayerActionSpec from XML files on classpath.
 * Pattern: classpath:narrative/player-actions/*.xml
 */
public class PlayerActionSpecLoader {

    private static final String PATTERN = "classpath:narrative/player-actions/*.xml";

    public List<PlayerActionSpec> loadAll() {
        List<PlayerActionSpec> specs = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(PATTERN);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    Document doc = builder.parse(is);
                    doc.getDocumentElement().normalize();
                    NodeList actionNodes = doc.getElementsByTagName("action");
                    for (int i = 0; i < actionNodes.getLength(); i++) {
                        specs.add(parseAction((Element) actionNodes.item(i)));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load player action specs from XML", e);
        }
        return specs;
    }

    private PlayerActionSpec parseAction(Element el) {
        String id = el.getAttribute("id");
        String code = el.getAttribute("code");
        int baseTimeCost = Integer.parseInt(el.getAttribute("time-cost"));

        String label = getTagText(el, "label", id);
        String description = getTagText(el, "description", "");
        String resultText = getTagText(el, "result-text", "");

        PlayerActionSpec.TimeCostSkillModifier skillMod = parseTimeCostSkill(el);
        PlayerActionSpec.StatEffects stats = parseStats(el);
        Map<String, Integer> relChanges = parseChanges(el, "relationships");
        Map<String, Integer> petChanges = parseChanges(el, "pet-mood");
        PlayerActionSpec.ActionFlags flags = parseFlags(el);
        Map<String, Integer> skillGains = parseSkillGains(el);
        PlayerActionSpec.JobEffects jobEffects = parseJobEffects(el);
        List<PlayerActionSpec.ExtraRelEffect> extraRels = parseExtraRelEffects(el);

        return new PlayerActionSpec(
                id, code, label, description, resultText,
                baseTimeCost, skillMod, stats, relChanges, petChanges, flags,
                skillGains, jobEffects, extraRels
        );
    }

    private PlayerActionSpec.TimeCostSkillModifier parseTimeCostSkill(Element parent) {
        NodeList nodes = parent.getElementsByTagName("time-cost-skill");
        if (nodes.getLength() == 0) return null;
        Element el = (Element) nodes.item(0);
        return new PlayerActionSpec.TimeCostSkillModifier(
                el.getAttribute("skill"),
                Double.parseDouble(attrOr(el, "reduction-per-level", "0")),
                Integer.parseInt(attrOr(el, "min-cost", "1"))
        );
    }

    private PlayerActionSpec.StatEffects parseStats(Element parent) {
        NodeList nodes = parent.getElementsByTagName("stats");
        if (nodes.getLength() == 0) {
            return new PlayerActionSpec.StatEffects(0, 0, 0, 0, 0, 0);
        }
        Element el = (Element) nodes.item(0);
        return new PlayerActionSpec.StatEffects(
                intAttr(el, "energy"),
                intAttr(el, "health"),
                intAttr(el, "stress"),
                intAttr(el, "mood"),
                intAttr(el, "money"),
                intAttr(el, "self-esteem")
        );
    }

    private Map<String, Integer> parseChanges(Element parent, String wrapperTag) {
        Map<String, Integer> map = new LinkedHashMap<>();
        NodeList wrappers = parent.getElementsByTagName(wrapperTag);
        if (wrappers.getLength() == 0) return map;
        Element wrapper = (Element) wrappers.item(0);
        NodeList changes = wrapper.getElementsByTagName("change");
        for (int i = 0; i < changes.getLength(); i++) {
            Element ch = (Element) changes.item(i);
            map.put(ch.getAttribute("target"), Integer.parseInt(ch.getAttribute("delta")));
        }
        return map;
    }

    private PlayerActionSpec.ActionFlags parseFlags(Element parent) {
        NodeList nodes = parent.getElementsByTagName("flags");
        if (nodes.getLength() == 0) {
            return new PlayerActionSpec.ActionFlags(false, false, Set.of(), false);
        }
        Element el = (Element) nodes.item(0);
        Set<String> interacted = new LinkedHashSet<>();
        String interactedStr = attrOr(el, "interacted-npcs", "");
        if (!interactedStr.isEmpty()) {
            for (String npc : interactedStr.split(",")) {
                interacted.add(npc.trim().toUpperCase());
            }
        }
        return new PlayerActionSpec.ActionFlags(
                Boolean.parseBoolean(attrOr(el, "rested", "false")),
                Boolean.parseBoolean(attrOr(el, "worked", "false")),
                interacted,
                Boolean.parseBoolean(attrOr(el, "reset-household-days", "false"))
        );
    }

    private Map<String, Integer> parseSkillGains(Element parent) {
        Map<String, Integer> map = new LinkedHashMap<>();
        NodeList wrappers = parent.getElementsByTagName("skill-gains");
        if (wrappers.getLength() == 0) return map;
        Element wrapper = (Element) wrappers.item(0);
        NodeList gains = wrapper.getElementsByTagName("skill");
        for (int i = 0; i < gains.getLength(); i++) {
            Element g = (Element) gains.item(i);
            map.put(g.getAttribute("name"), Integer.parseInt(g.getAttribute("xp")));
        }
        return map;
    }

    private PlayerActionSpec.JobEffects parseJobEffects(Element parent) {
        NodeList nodes = parent.getElementsByTagName("job-effects");
        if (nodes.getLength() == 0) return new PlayerActionSpec.JobEffects(0, 0);
        Element el = (Element) nodes.item(0);
        return new PlayerActionSpec.JobEffects(
                intAttr(el, "satisfaction"),
                intAttr(el, "burnout-risk")
        );
    }

    private List<PlayerActionSpec.ExtraRelEffect> parseExtraRelEffects(Element parent) {
        List<PlayerActionSpec.ExtraRelEffect> list = new ArrayList<>();
        NodeList wrappers = parent.getElementsByTagName("extra-relationship-effects");
        if (wrappers.getLength() == 0) return list;
        Element wrapper = (Element) wrappers.item(0);
        NodeList effects = wrapper.getElementsByTagName("effect");
        for (int i = 0; i < effects.getLength(); i++) {
            Element e = (Element) effects.item(i);
            list.add(new PlayerActionSpec.ExtraRelEffect(
                    e.getAttribute("target"),
                    intAttr(e, "closeness"),
                    intAttr(e, "trust"),
                    intAttr(e, "stability"),
                    intAttr(e, "romance")
            ));
        }
        return list;
    }

    private String getTagText(Element parent, String tag, String defaultValue) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return defaultValue;
    }

    private String attrOr(Element el, String attr, String defaultValue) {
        String val = el.getAttribute(attr);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    private int intAttr(Element el, String attr) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return 0;
        return Integer.parseInt(val);
    }
}
