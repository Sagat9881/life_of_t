package ru.lifegame.backend.domain.engine.parser;

import ru.lifegame.backend.domain.engine.spec.EventSpec;
import ru.lifegame.backend.domain.engine.spec.EventSpec.*;
import ru.lifegame.backend.domain.engine.spec.ConditionSpec;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.util.*;

public class EventSpecParser {

    public EventSpec parse(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        EventMeta meta = parseMeta(root);
        List<ConditionSpec> triggers = parseConditions(root, "triggers");
        List<OptionSpec> options = parseOptions(root);

        return new EventSpec(id, meta, triggers, options);
    }

    private EventMeta parseMeta(Element root) {
        String type = root.getAttribute("type");
        String category = root.hasAttribute("category") ? root.getAttribute("category") : "";
        int priority = root.hasAttribute("priority") ? Integer.parseInt(root.getAttribute("priority")) : 5;
        boolean repeatable = Boolean.parseBoolean(root.getAttribute("repeatable"));
        int cooldown = root.hasAttribute("cooldown-days") ? Integer.parseInt(root.getAttribute("cooldown-days")) : 0;
        return new EventMeta(type, category, priority, repeatable, cooldown);
    }

    private List<ConditionSpec> parseConditions(Element root, String parentTag) {
        List<ConditionSpec> conditions = new ArrayList<>();
        NodeList parents = root.getElementsByTagName(parentTag);
        if (parents.getLength() > 0) {
            Element parent = (Element) parents.item(0);
            NodeList nodes = parent.getElementsByTagName("condition");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                conditions.add(new ConditionSpec(
                    el.getAttribute("type"),
                    el.hasAttribute("axis") ? el.getAttribute("axis") : el.getAttribute("target"),
                    el.getAttribute("operator"),
                    el.hasAttribute("value") ? el.getAttribute("value") : el.getAttribute("min")
                ));
            }
        }
        return conditions;
    }

    private List<OptionSpec> parseOptions(Element root) {
        List<OptionSpec> options = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("option");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            EffectSpec effects = parseEffects(el);
            options.add(new OptionSpec(
                el.getAttribute("id"),
                el.getAttribute("text"),
                el.getAttribute("result"),
                effects
            ));
        }
        return options;
    }

    private EffectSpec parseEffects(Element option) {
        int energy = parseIntAttr(option, "energy");
        int stress = parseIntAttr(option, "stress");
        int mood = parseIntAttr(option, "mood");
        int money = parseIntAttr(option, "money");
        return new EffectSpec(energy, stress, mood, money, Map.of(), Map.of());
    }

    private int parseIntAttr(Element el, String attr) {
        String val = el.getAttribute(attr);
        if (val == null || val.isEmpty()) return 0;
        return Integer.parseInt(val);
    }
}
