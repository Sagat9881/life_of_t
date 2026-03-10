package ru.lifegame.backend.domain.narrative.parser;

import ru.lifegame.backend.domain.narrative.spec.QuestSpec;
import ru.lifegame.backend.domain.narrative.spec.QuestSpec.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Parses quest XML files from classpath:narrative/quests/**\/*.xml.
 *
 * Two supported root formats:
 *   <quest id="...">...</quest>         — single-quest file (preferred, 1 file per quest)
 *   <quests><quest>...</quest></quests> — legacy container (kept for test convenience)
 *
 * Public API:
 *   parseOne(InputStream, filename)  — parses exactly 1 quest from a single-quest file
 *   parseAll(InputStream, filename)  — parses 1..N quests from any supported format
 *   parseAll(File)                   — convenience overload for unit tests
 */
public class QuestSpecParser {

    /**
     * Parses a single-quest file (root element is {@code <quest>}).
     * Throws if the file contains zero or more than one quest element.
     */
    public QuestSpec parseOne(InputStream xmlStream, String filename) throws Exception {
        List<QuestSpec> all = parseAll(xmlStream, filename);
        if (all.isEmpty()) {
            throw new IllegalArgumentException(
                    "No <quest> element found in quest file: " + filename);
        }
        if (all.size() > 1) {
            throw new IllegalArgumentException(
                    "Expected exactly 1 <quest> in file " + filename +
                    ", but found " + all.size() +
                    ". Use parseAll() for multi-quest files.");
        }
        return all.get(0);
    }

    /**
     * Parses all quests from an XML stream.
     * Handles both {@code <quest>} root and {@code <quests>} container root.
     */
    public List<QuestSpec> parseAll(InputStream xmlStream, String filename) throws Exception {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            return parseDocument(f.newDocumentBuilder().parse(xmlStream));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse quest XML: " + filename, e);
        }
    }

    /**
     * Convenience overload for unit tests — parses from a {@link File}.
     */
    public List<QuestSpec> parseAll(File xmlFile) throws Exception {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            return parseDocument(f.newDocumentBuilder().parse(xmlFile));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse quest XML: " + xmlFile.getName(), e);
        }
    }

    // ── internals ───────────────────────────────────────────────────────────

    private List<QuestSpec> parseDocument(Document doc) {
        doc.getDocumentElement().normalize();
        NodeList questNodes = doc.getElementsByTagName("quest");
        List<QuestSpec> result = new ArrayList<>();
        for (int i = 0; i < questNodes.getLength(); i++) {
            result.add(parseQuest((Element) questNodes.item(i)));
        }
        return result;
    }

    private QuestSpec parseQuest(Element el) {
        String  id        = el.getAttribute("id");
        String  type      = el.getAttribute("type");
        boolean autoStart = "true".equalsIgnoreCase(el.getAttribute("auto-start"));
        int     triggerDay = autoStart
                ? parseIntAttr(el, "trigger-day", 0)
                : parseIntAttr(el, "trigger-day", 1);

        String label       = directChildText(el, "label");
        String description = directChildText(el, "description");
        List<String> requiredNpcs = parseRequiredNpcs(el);
        QuestMeta meta = new QuestMeta(label, description, type, triggerDay, autoStart, requiredNpcs);

        List<StepSpec> steps = parseSteps(el);

        List<RewardSpec> questRewards = parseQuestRewards(el);
        if (!questRewards.isEmpty() && !steps.isEmpty()) {
            int last = steps.size() - 1;
            StepSpec ls = steps.get(last);
            List<RewardSpec> merged = new ArrayList<>(ls.rewards());
            merged.addAll(questRewards);
            steps.set(last, new StepSpec(ls.stepId(), ls.description(), ls.objectives(), merged, ls.dialogueText()));
        }

        return new QuestSpec(id, meta, steps);
    }

    private List<StepSpec> parseSteps(Element questEl) {
        List<StepSpec> steps = new ArrayList<>();
        NodeList stepsContainers = questEl.getElementsByTagName("steps");
        if (stepsContainers.getLength() == 0) return steps;
        NodeList stepNodes = ((Element) stepsContainers.item(0)).getElementsByTagName("step");
        for (int i = 0; i < stepNodes.getLength(); i++) {
            steps.add(parseStep((Element) stepNodes.item(i)));
        }
        return steps;
    }

    private StepSpec parseStep(Element el) {
        String stepId   = el.getAttribute("id");
        String stepType = el.getAttribute("type");
        String label    = directChildText(el, "label");

        List<ObjectiveSpec> objectives = new ArrayList<>();
        if ("compound".equals(stepType)) {
            NodeList conditions = el.getElementsByTagName("condition");
            for (int i = 0; i < conditions.getLength(); i++) {
                Element c       = (Element) conditions.item(i);
                String condType = c.getAttribute("type");
                String field    = c.getAttribute("field");
                String operator = c.getAttribute("operator");
                String value    = c.getAttribute("value");
                String target   = "relationship".equals(condType)
                        ? c.getAttribute("target") + ":" + field
                        : field;
                objectives.add(new ObjectiveSpec(condType, target, operator, value));
            }
        } else {
            String target    = directChildText(el, "target");
            String threshold = directChildText(el, "threshold");
            if (!target.isBlank() && !threshold.isBlank()) {
                objectives.add(new ObjectiveSpec(stepType, target, "gte", threshold));
            }
        }

        List<RewardSpec> rewards = parseRewardElements(el);
        String dialogue = directChildText(el, "dialogue");
        return new StepSpec(stepId, label, objectives, rewards, dialogue);
    }

    private List<RewardSpec> parseQuestRewards(Element questEl) {
        List<RewardSpec> rewards = new ArrayList<>();
        NodeList containers = questEl.getElementsByTagName("rewards");
        if (containers.getLength() == 0) return rewards;
        Element rewardsEl = (Element) containers.item(0);

        NodeList statEffects = rewardsEl.getElementsByTagName("stat-effect");
        for (int i = 0; i < statEffects.getLength(); i++) {
            Element r = (Element) statEffects.item(i);
            rewards.add(new RewardSpec("stat", r.getAttribute("field"), parseIntAttr(r, "delta", 0)));
        }
        NodeList relEffects = rewardsEl.getElementsByTagName("relationship-effect");
        for (int i = 0; i < relEffects.getLength(); i++) {
            Element r = (Element) relEffects.item(i);
            rewards.add(new RewardSpec("relationship",
                    r.getAttribute("target") + ":" + r.getAttribute("field"),
                    parseIntAttr(r, "delta", 0)));
        }
        NodeList achievements = rewardsEl.getElementsByTagName("achievement");
        for (int i = 0; i < achievements.getLength(); i++) {
            Element r = (Element) achievements.item(i);
            rewards.add(new RewardSpec("achievement", r.getAttribute("id"), 0));
        }
        return rewards;
    }

    private List<RewardSpec> parseRewardElements(Element parent) {
        List<RewardSpec> rewards = new ArrayList<>();
        NodeList nodes = parent.getElementsByTagName("reward");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element r = (Element) nodes.item(i);
            rewards.add(new RewardSpec(r.getAttribute("type"), r.getAttribute("target"),
                    parseIntAttr(r, "amount", 0)));
        }
        return rewards;
    }

    private List<String> parseRequiredNpcs(Element root) {
        String text = directChildText(root, "required-npcs");
        if (text.isBlank()) return List.of();
        return Arrays.stream(text.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private String directChildText(Element parent, String tagName) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getNodeName())) {
                return child.getTextContent().trim();
            }
        }
        return "";
    }

    private int parseIntAttr(Element el, String attr, int defaultVal) {
        String val = el.getAttribute(attr);
        return (val == null || val.isEmpty()) ? defaultVal : Integer.parseInt(val);
    }
}
