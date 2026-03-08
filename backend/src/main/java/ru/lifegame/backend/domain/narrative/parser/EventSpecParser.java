package ru.lifegame.backend.domain.narrative.parser;

import ru.lifegame.backend.domain.npc.spec.EventSpec;
import ru.lifegame.backend.domain.npc.spec.EventSpec.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class EventSpecParser {

    public EventSpec parse(File xmlFile) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        String title = getTextContent(root, "title");
        String description = getTextContent(root, "description");
        String type = root.getAttribute("type");
        int priority = parseIntAttr(root, "priority", 5);
        boolean repeatable = "true".equals(root.getAttribute("repeatable"));
        int cooldown = parseIntAttr(root, "cooldown-days", 0);

        EventMeta meta = new EventMeta(title, description, type, priority, repeatable, cooldown);

        List<ConditionSpec> triggers = new ArrayList<>();
        NodeList condNodes = root.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element el = (Element) condNodes.item(i);
            if ("trigger".equals(((Element) el.getParentNode()).getTagName()) || "triggers".equals(((Element) el.getParentNode()).getTagName())) {
                triggers.add(new ConditionSpec(el.getAttribute("type"), el.getAttribute("target"), el.getAttribute("operator"), el.getAttribute("value")));
            }
        }

        List<OptionSpec> options = new ArrayList<>();
        NodeList optNodes = root.getElementsByTagName("option");
        for (int i = 0; i < optNodes.getLength(); i++) {
            Element el = (Element) optNodes.item(i);
            List<EffectSpec> effects = new ArrayList<>();
            NodeList effNodes = el.getElementsByTagName("effect");
            for (int j = 0; j < effNodes.getLength(); j++) {
                Element eff = (Element) effNodes.item(j);
                effects.add(new EffectSpec(eff.getAttribute("target"), eff.getAttribute("stat"), parseIntAttr(eff, "delta", 0)));
            }
            options.add(new OptionSpec(el.getAttribute("id"), el.getAttribute("text"), el.getAttribute("result"), effects));
        }

        return new EventSpec(id, meta, triggers, options);
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
