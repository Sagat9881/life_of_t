package ru.lifegame.backend.domain.action.spec;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlayerActionSpecLoader {

    private static final String PATTERN = "classpath:narrative/actions/**/*.xml";

    public List<PlayerActionSpec> loadAll() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(PATTERN);
            if (resources.length == 0) {
                throw new IllegalStateException(
                        "No action XML files found at classpath:narrative/actions/ — game cannot start");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Map<String, PlayerActionSpec> byCode = new LinkedHashMap<>();
            for (Resource res : resources) {
                String filename = res.getFilename();
                try (InputStream is = res.getInputStream()) {
                    Document doc = builder.parse(is);
                    doc.getDocumentElement().normalize();
                    PlayerActionSpec spec = parseAction(doc.getDocumentElement(), filename);
                    byCode.putIfAbsent(spec.code(), spec);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Failed to parse action file: " + filename + " — game cannot start", e);
                }
            }
            return List.copyOf(byCode.values());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to scan classpath:narrative/actions/ — game cannot start", e);
        }
    }

    private PlayerActionSpec parseAction(Element el, String filename) {
        String code = el.getAttribute("code");
        if (code == null || code.isBlank()) {
            throw new IllegalStateException(
                    "Missing required attribute 'code' in action file: " + filename);
        }
        int timeCost = Integer.parseInt(el.getAttribute("time-cost").isBlank() ? "0" : el.getAttribute("time-cost"));
        String label            = textOf(el, "label");
        String description      = textOf(el, "description");
        String icon             = textOf(el, "icon");
        String animationTrigger = textOf(el, "animation-trigger");

        PlayerActionSpec.StatEffects stats = parseStats(el);
        Map<String, Integer> skillGains          = parseSkillGains(el);
        Map<String, Integer> relationshipChanges = parseChanges(el, "relationships");
        Map<String, Integer> petMoodChanges      = parseChanges(el, "pet-mood");
        List<String> tags      = parseTextList(el, "tags",       "tag");
        List<String> timeSlots = parseTextList(el, "time-slots", "slot");
        List<String> locations = parseTextList(el, "locations",  "location");

        PlayerActionSpec.Flags flags           = parseFlags(el);
        PlayerActionSpec.JobEffects jobEffects = parseJobEffects(el);
        List<PlayerActionSpec.ExtraRelEffect> extraRelEffects = parseExtraRelEffects(el);

        return new PlayerActionSpec(
                code, label, description, icon, animationTrigger,
                timeCost, stats, skillGains, relationshipChanges, petMoodChanges,
                tags, timeSlots, locations,
                flags, jobEffects, extraRelEffects
        );
    }

    private PlayerActionSpec.StatEffects parseStats(Element el) {
        Element statsEl = firstChild(el, "stats");
        if (statsEl == null) {
            return new PlayerActionSpec.StatEffects(0, 0, 0, 0, 0, 0);
        }
        return new PlayerActionSpec.StatEffects(
                intAttr(statsEl, "energy"),
                intAttr(statsEl, "health"),
                intAttr(statsEl, "stress"),
                intAttr(statsEl, "mood"),
                intAttr(statsEl, "money"),
                intAttr(statsEl, "self-esteem")
        );
    }

    private PlayerActionSpec.Flags parseFlags(Element el) {
        Element flagsEl = firstChild(el, "flags");
        if (flagsEl == null) return PlayerActionSpec.Flags.defaults();
        boolean resetHousehold = boolAttr(flagsEl, "reset-household-days");
        return new PlayerActionSpec.Flags(resetHousehold);
    }

    private PlayerActionSpec.JobEffects parseJobEffects(Element el) {
        Element jobEl = firstChild(el, "job-effects");
        if (jobEl == null) return PlayerActionSpec.JobEffects.none();
        int satisfaction = intAttr(jobEl, "satisfaction");
        int burnoutRisk  = intAttr(jobEl, "burnout-risk");
        return new PlayerActionSpec.JobEffects(satisfaction, burnoutRisk);
    }

    private List<PlayerActionSpec.ExtraRelEffect> parseExtraRelEffects(Element el) {
        Element wrapper = firstChild(el, "extra-relationship-effects");
        if (wrapper == null) return List.of();
        NodeList nodes = wrapper.getElementsByTagName("effect");
        List<PlayerActionSpec.ExtraRelEffect> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            result.add(new PlayerActionSpec.ExtraRelEffect(
                    e.getAttribute("target"),
                    intAttr(e, "closeness"),
                    intAttr(e, "trust"),
                    intAttr(e, "stability"),
                    intAttr(e, "romance")
            ));
        }
        return List.copyOf(result);
    }

    private Map<String, Integer> parseSkillGains(Element el) {
        Element skillsEl = firstChild(el, "skills");
        if (skillsEl == null) return Map.of();
        NodeList nodes = skillsEl.getElementsByTagName("skill");
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element s = (Element) nodes.item(i);
            String name = s.getAttribute("name");
            int xp = intAttr(s, "xp");
            if (!name.isBlank()) result.put(name, xp);
        }
        return Map.copyOf(result);
    }

    private Map<String, Integer> parseChanges(Element el, String wrapperTag) {
        Element wrapper = firstChild(el, wrapperTag);
        if (wrapper == null) return Map.of();
        NodeList nodes = wrapper.getChildNodes();
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (!(nodes.item(i) instanceof Element child)) continue;
            String key = child.getTagName();
            String val = child.getTextContent().trim();
            if (!key.isBlank() && !val.isBlank()) {
                result.put(key, Integer.parseInt(val));
            }
        }
        return Map.copyOf(result);
    }

    private List<String> parseTextList(Element parent, String wrapperTag, String itemTag) {
        Element wrapper = firstChild(parent, wrapperTag);
        if (wrapper == null) return List.of();
        NodeList nodes = wrapper.getElementsByTagName(itemTag);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            String text = nodes.item(i).getTextContent().trim();
            if (!text.isEmpty()) result.add(text);
        }
        return List.copyOf(result);
    }

    private int intAttr(Element el, String attr) {
        String val = el.getAttribute(attr);
        if (val == null || val.isBlank()) return 0;
        return Integer.parseInt(val);
    }

    private boolean boolAttr(Element el, String attr) {
        String val = el.getAttribute(attr);
        return "true".equalsIgnoreCase(val);
    }

    private String textOf(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return "";
        return nodes.item(0).getTextContent().trim();
    }

    private Element firstChild(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }
}
