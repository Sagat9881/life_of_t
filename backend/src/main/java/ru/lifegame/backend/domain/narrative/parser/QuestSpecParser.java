package ru.lifegame.backend.domain.narrative.parser;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Parses a single narrative/quests/*.xml file into a {@link QuestSpec}.
 *
 * Two entry points:
 *   - {@link #parse(InputStream, String)} — preferred; works inside JARs
 *   - {@link #parse(File)}               — delegates to the above; kept for tests
 */
public class QuestSpecParser {

    // ── public API ───────────────────────────────────────────────────────────

    /** Preferred entry point — works both on filesystem and inside JARs. */
    public QuestSpec parse(InputStream xmlStream, String filename) throws Exception {
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse quest XML: " + filename, e);
        }
        return parseDocument(doc);
    }

    /** Convenience overload for filesystem files (e.g. unit tests). */
    public QuestSpec parse(File xmlFile) throws Exception {
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse quest XML: " + xmlFile.getName(), e);
        }
        return parseDocument(doc);
    }

    // ── core parsing ─────────────────────────────────────────────────────────

    private QuestSpec parseDocument(Document doc) {
        Element root = doc.getDocumentElement();

        String id          = root.getAttribute("id");
        String title       = getTextContent(root, "title");
        String description = getTextContent(root, "description");
        String type        = root.getAttribute("type");
        int    triggerDay  = parseIntAttr(root, "trigger-day", 1);
        List<String> requiredNpcs = parseRequiredNpcs(root);

        QuestMeta meta = new QuestMeta(title, description, type, triggerDay, requiredNpcs);

        List<StepSpec> steps = new ArrayList<>();
        NodeList stepNodes = root.getElementsByTagName("step");
        for (int i = 0; i < stepNodes.getLength(); i++) {
            Element el       = (Element) stepNodes.item(i);
            String  stepId   = el.getAttribute("id");
            String  stepDesc = getTextContent(el, "description");

            List<ObjectiveSpec> objectives = new ArrayList<>();
            NodeList objNodes = el.getElementsByTagName("objective");
            for (int j = 0; j < objNodes.getLength(); j++) {
                Element obj = (Element) objNodes.item(j);
                objectives.add(new ObjectiveSpec(
                        obj.getAttribute("type"),
                        obj.getAttribute("target"),
                        obj.getAttribute("operator"),
                        obj.getAttribute("value")
                ));
            }

            List<RewardSpec> rewards = new ArrayList<>();
            NodeList rewNodes = el.getElementsByTagName("reward");
            for (int j = 0; j < rewNodes.getLength(); j++) {
                Element rew = (Element) rewNodes.item(j);
                rewards.add(new RewardSpec(
                        rew.getAttribute("type"),
                        rew.getAttribute("target"),
                        parseIntAttr(rew, "amount", 0)
                ));
            }

            String dialogue = getTextContent(el, "dialogue");
            steps.add(new StepSpec(stepId, stepDesc, objectives, rewards, dialogue));
        }

        return new QuestSpec(id, meta, steps);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private List<String> parseRequiredNpcs(Element root) {
        String text = getTextContent(root, "required-npcs");
        if (text.isBlank()) return List.of();
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private String getTextContent(Element root, String tagName) {
        NodeList nodes = root.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private int parseIntAttr(Element el, String attr, int defaultVal) {
        String val = el.getAttribute(attr);
        return val.isEmpty() ? defaultVal : Integer.parseInt(val);
    }
}
