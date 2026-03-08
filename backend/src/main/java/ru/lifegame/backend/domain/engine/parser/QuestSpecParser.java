package ru.lifegame.backend.application.engine.parser;

import com.sagat.life_of_t.domain.engine.spec.QuestSpec;
import com.sagat.life_of_t.domain.engine.spec.QuestSpec.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public class QuestSpecParser {

    public QuestSpec parse(InputStream xml) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        QuestMeta meta = parseMeta(root);
        List<ObjectiveSpec> objectives = parseObjectives(root);
        List<StepSpec> steps = parseSteps(root);
        List<RewardSpec> rewards = parseRewards(root);

        return new QuestSpec(id, type, meta, objectives, steps, rewards);
    }

    private QuestMeta parseMeta(Element root) {
        Element m = (Element) root.getElementsByTagName("meta").item(0);
        return new QuestMeta(
                textContent(m, "title"),
                textContent(m, "title-ru"),
                textContent(m, "description"),
                textContent(m, "description-ru"),
                parseIntOrDefault(textContent(m, "chapter"), 1),
                textContent(m, "prerequisites")
        );
    }

    private List<ObjectiveSpec> parseObjectives(Element root) {
        List<ObjectiveSpec> list = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("objective");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            list.add(new ObjectiveSpec(
                    el.getAttribute("id"),
                    el.getAttribute("type"),
                    textContent(el, "description-ru"),
                    textContent(el, "target"),
                    textContent(el, "location"),
                    parseIntOrDefault(textContent(el, "count"), 1)
            ));
        }
        return Collections.unmodifiableList(list);
    }

    private List<StepSpec> parseSteps(Element root) {
        List<StepSpec> list = new ArrayList<>();
        NodeList stepNodes = root.getElementsByTagName("step");
        for (int i = 0; i < stepNodes.getLength(); i++) {
            Element el = (Element) stepNodes.item(i);
            int order = Integer.parseInt(el.getAttribute("order"));
            String objRef = el.getAttribute("objective-ref");

            List<DialogueEntry> dialogues = new ArrayList<>();
            NodeList dlgNodes = el.getElementsByTagName("dialogue");
            for (int j = 0; j < dlgNodes.getLength(); j++) {
                Element d = (Element) dlgNodes.item(j);
                String speaker = d.getAttribute("speaker");
                String lineRu = textContent(d, "line-ru");
                String choice = d.hasAttribute("choice") ? d.getAttribute("choice") : null;
                dialogues.add(new DialogueEntry(speaker, lineRu, choice));
            }

            List<RewardSpec> onComplete = new ArrayList<>();
            NodeList ocNodes = el.getElementsByTagName("on-complete");
            if (ocNodes.getLength() > 0) {
                Element oc = (Element) ocNodes.item(0);
                onComplete.addAll(parseRewardElements(oc));
            }

            list.add(new StepSpec(order, objRef, Collections.unmodifiableList(dialogues),
                    Collections.unmodifiableList(onComplete)));
        }
        return Collections.unmodifiableList(list);
    }

    private List<RewardSpec> parseRewards(Element root) {
        NodeList rewardNodes = root.getElementsByTagName("rewards");
        if (rewardNodes.getLength() == 0) return List.of();
        return Collections.unmodifiableList(parseRewardElements((Element) rewardNodes.item(0)));
    }

    private List<RewardSpec> parseRewardElements(Element parent) {
        List<RewardSpec> list = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element el)) continue;
            String tag = el.getTagName();
            switch (tag) {
                case "stat-change" -> list.add(new RewardSpec(
                        "stat-change", el.getAttribute("stat"), null,
                        el.getAttribute("value"),
                        el.hasAttribute("condition") ? el.getAttribute("condition") : null,
                        null, null));
                case "relationship-change" -> list.add(new RewardSpec(
                        "relationship-change", null, el.getAttribute("target"),
                        el.getAttribute("value"),
                        el.hasAttribute("condition") ? el.getAttribute("condition") : null,
                        null, null));
                case "unlock" -> list.add(new RewardSpec(
                        "unlock", null, null, null, null,
                        el.getAttribute("type"), el.getTextContent().trim()));
            }
        }
        return list;
    }

    private String textContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private int parseIntOrDefault(String s, int def) {
        if (s == null || s.isBlank()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
