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
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        var root = doc.getDocumentElement();
        String id = root.getAttribute("id");
        QuestMeta meta = parseMeta(root);
        List<StepSpec> steps = parseSteps(root);
        List<RewardSpec> rewards = parseRewards(root, "completion-rewards");
        return new QuestSpec(id, meta, steps, rewards);
    }

    private QuestMeta parseMeta(Element root) {
        String title = getTextContent(root, "title");
        String desc = getTextContent(root, "description");
        String category = root.getAttribute("category");
        int triggerDay = intAttr(root, "trigger-day");
        return new QuestMeta(title, desc, category, triggerDay, List.of());
    }

    private List<StepSpec> parseSteps(Element root) {
        List<StepSpec> steps = new ArrayList<>();
        var nodes = root.getElementsByTagName("step");
        for (int i = 0; i < nodes.getLength(); i++) {
            var el = (Element) nodes.item(i);
            List<ObjectiveSpec> objectives = parseObjectives(el);
            List<RewardSpec> rewards = parseRewards(el, "rewards");
            DialogueEntry dialogue = parseDialogue(el);
            steps.add(new StepSpec(el.getAttribute("id"), el.getAttribute("type"), el.getAttribute("description"), objectives, rewards, dialogue));
        }
        return steps;
    }

    private List<ObjectiveSpec> parseObjectives(Element parent) {
        List<ObjectiveSpec> list = new ArrayList<>();
        var nodes = parent.getElementsByTagName("objective");
        for (int i = 0; i < nodes.getLength(); i++) {
            var el = (Element) nodes.item(i);
            list.add(new ObjectiveSpec(el.getAttribute("type"), el.getAttribute("target"), intAttr(el, "count"), List.of()));
        }
        return list;
    }

    private List<RewardSpec> parseRewards(Element parent, String containerTag) {
        List<RewardSpec> list = new ArrayList<>();
        var containers = parent.getElementsByTagName(containerTag);
        if (containers.getLength() > 0) {
            var nodes = ((Element) containers.item(0)).getElementsByTagName("reward");
            for (int i = 0; i < nodes.getLength(); i++) {
                var el = (Element) nodes.item(i);
                list.add(new RewardSpec(el.getAttribute("type"), el.getAttribute("target"), intAttr(el, "value")));
            }
        }
        return list;
    }

    private DialogueEntry parseDialogue(Element parent) {
        var nodes = parent.getElementsByTagName("dialogue");
        if (nodes.getLength() > 0) {
            var el = (Element) nodes.item(0);
            return new DialogueEntry(el.getAttribute("speaker"), el.getTextContent().trim(), List.of());
        }
        return null;
    }

    private String getTextContent(Element root, String tag) {
        var nodes = root.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private int intAttr(Element el, String name) {
        String val = el.getAttribute(name);
        if (val == null || val.isEmpty()) return 0;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return 0; }
    }
}
