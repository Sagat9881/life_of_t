package ru.lifegame.backend.domain.engine.parser;

import ru.lifegame.backend.domain.engine.spec.QuestSpec;
import ru.lifegame.backend.domain.engine.spec.QuestSpec.*;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.util.*;

public class QuestSpecParser {

    public QuestSpec parse(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        QuestMeta meta = parseMeta(root);
        List<StepSpec> steps = parseSteps(root);

        return new QuestSpec(id, meta, steps);
    }

    private QuestMeta parseMeta(Element root) {
        String title = getTextContent(root, "title");
        String description = getTextContent(root, "description");
        String category = root.hasAttribute("category") ? root.getAttribute("category") : "";
        int triggerDay = root.hasAttribute("trigger-day") ? Integer.parseInt(root.getAttribute("trigger-day")) : 1;
        List<ConditionSpec> prerequisites = new ArrayList<>();
        NodeList preNodes = root.getElementsByTagName("prerequisites");
        if (preNodes.getLength() > 0) {
            Element preEl = (Element) preNodes.item(0);
            NodeList condNodes = preEl.getElementsByTagName("condition");
            for (int i = 0; i < condNodes.getLength(); i++) {
                Element el = (Element) condNodes.item(i);
                prerequisites.add(new ConditionSpec(el.getAttribute("type"), el.getAttribute("target"), el.getAttribute("operator"), el.getAttribute("value")));
            }
        }
        return new QuestMeta(title, description, category, triggerDay, prerequisites);
    }

    private List<StepSpec> parseSteps(Element root) {
        List<StepSpec> steps = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("step");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String sid = el.getAttribute("id");
            String desc = getTextContent(el, "description");
            List<ObjectiveSpec> objectives = parseObjectives(el);
            DialogueEntry dialogue = parseDialogue(el);
            RewardSpec reward = parseReward(el);
            steps.add(new StepSpec(sid, desc, objectives, dialogue, reward));
        }
        return steps;
    }

    private List<ObjectiveSpec> parseObjectives(Element stepEl) {
        List<ObjectiveSpec> objectives = new ArrayList<>();
        NodeList nodes = stepEl.getElementsByTagName("objective");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            objectives.add(new ObjectiveSpec(
                el.getAttribute("type"),
                el.getAttribute("target"),
                el.hasAttribute("count") ? Integer.parseInt(el.getAttribute("count")) : 1,
                List.of()
            ));
        }
        return objectives;
    }

    private DialogueEntry parseDialogue(Element stepEl) {
        NodeList nodes = stepEl.getElementsByTagName("dialogue");
        if (nodes.getLength() > 0) {
            Element el = (Element) nodes.item(0);
            return new DialogueEntry(el.getAttribute("speaker"), el.getTextContent().trim(), List.of());
        }
        return new DialogueEntry("", "", List.of());
    }

    private RewardSpec parseReward(Element stepEl) {
        NodeList nodes = stepEl.getElementsByTagName("reward");
        if (nodes.getLength() > 0) {
            Element el = (Element) nodes.item(0);
            return new RewardSpec(
                parseIntAttr(el, "energy"), parseIntAttr(el, "stress"),
                parseIntAttr(el, "mood"), parseIntAttr(el, "money"),
                Map.of(), Map.of()
            );
        }
        return new RewardSpec(0, 0, 0, 0, Map.of(), Map.of());
    }

    private String getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private int parseIntAttr(Element el, String attr) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return 0;
        return Integer.parseInt(val);
    }
}
