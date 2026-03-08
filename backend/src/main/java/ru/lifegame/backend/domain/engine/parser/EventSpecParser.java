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
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        var root = doc.getDocumentElement();
        String id = root.getAttribute("id");
        EventMeta meta = parseMeta(root);
        List<ConditionSpec> triggers = parseConditions(root, "trigger");
        List<OptionSpec> options = parseOptions(root);
        return new EventSpec(id, meta, triggers, options);
    }

    private EventMeta parseMeta(Element root) {
        String type = root.getAttribute("type");
        String category = root.getAttribute("category");
        int priority = intAttr(root, "priority");
        boolean repeatable = Boolean.parseBoolean(root.getAttribute("repeatable"));
        int cooldown = intAttr(root, "cooldown-days");
        return new EventMeta(type, category, priority, repeatable, cooldown);
    }

    private List<ConditionSpec> parseConditions(Element root, String parentTag) {
        List<ConditionSpec> list = new ArrayList<>();
        var parentNodes = root.getElementsByTagName(parentTag);
        if (parentNodes.getLength() > 0) {
            var conditions = ((Element) parentNodes.item(0)).getElementsByTagName("condition");
            for (int i = 0; i < conditions.getLength(); i++) {
                var el = (Element) conditions.item(i);
                list.add(new ConditionSpec(el.getAttribute("type"), el.getAttribute("target"), el.getAttribute("operator"), el.getAttribute("value")));
            }
        }
        return list;
    }

    private List<OptionSpec> parseOptions(Element root) {
        List<OptionSpec> list = new ArrayList<>();
        var nodes = root.getElementsByTagName("option");
        for (int i = 0; i < nodes.getLength(); i++) {
            var el = (Element) nodes.item(i);
            if (el.getParentNode().getNodeName().equals("options")) {
                EffectSpec effects = new EffectSpec(intAttr(el, "energy"), intAttr(el, "stress"), intAttr(el, "mood"), intAttr(el, "money"), Map.of(), Map.of());
                list.add(new OptionSpec(el.getAttribute("id"), el.getAttribute("text"), el.getAttribute("result"), effects));
            }
        }
        return list;
    }

    private int intAttr(Element el, String name) {
        String val = el.getAttribute(name);
        if (val == null || val.isEmpty()) return 0;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return 0; }
    }
}
