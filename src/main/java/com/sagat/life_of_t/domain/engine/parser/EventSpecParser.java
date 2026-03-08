package com.sagat.life_of_t.domain.engine.parser;

import com.sagat.life_of_t.domain.engine.spec.EventSpec;
import com.sagat.life_of_t.domain.engine.spec.EventSpec.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public class EventSpecParser {

    public EventSpec parse(InputStream xml) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        String type = root.getAttribute("type");

        EventMeta meta = parseMeta(root);
        List<ConditionSpec> conditions = parseConditions(root);
        List<EffectSpec> effects = parseEffects(root);

        return new EventSpec(id, type, meta, conditions, effects);
    }

    private EventMeta parseMeta(Element root) {
        Element metaEl = (Element) root.getElementsByTagName("meta").item(0);
        String titleRu = textContent(metaEl, "title-ru");
        String descRu = textContent(metaEl, "description-ru");
        double prob = Double.parseDouble(textContent(metaEl, "probability"));
        int cooldown = Integer.parseInt(textContent(metaEl, "cooldown-hours"));
        return new EventMeta(titleRu, descRu, prob, cooldown);
    }

    private List<ConditionSpec> parseConditions(Element root) {
        List<ConditionSpec> list = new ArrayList<>();
        NodeList condNodes = root.getElementsByTagName("condition");
        for (int i = 0; i < condNodes.getLength(); i++) {
            Element el = (Element) condNodes.item(i);
            String type = el.getAttribute("type");
            String value = el.getAttribute("value");
            Map<String, String> attrs = new LinkedHashMap<>();
            NamedNodeMap attrMap = el.getAttributes();
            for (int j = 0; j < attrMap.getLength(); j++) {
                Attr a = (Attr) attrMap.item(j);
                if (!"type".equals(a.getName()) && !"value".equals(a.getName()))
                    attrs.put(a.getName(), a.getValue());
            }
            list.add(new ConditionSpec(type, value, Collections.unmodifiableMap(attrs)));
        }
        return Collections.unmodifiableList(list);
    }

    private List<EffectSpec> parseEffects(Element root) {
        List<EffectSpec> effects = new ArrayList<>();
        NodeList effectsNode = root.getElementsByTagName("effects");
        if (effectsNode.getLength() == 0) return effects;
        Element effectsEl = (Element) effectsNode.item(0);

        NodeList children = effectsEl.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element el)) continue;
            String tag = el.getTagName();
            switch (tag) {
                case "stat-change" -> effects.add(new EffectSpec(
                        "stat-change", el.getAttribute("stat"), null,
                        el.getAttribute("value"), null, null, null));
                case "relationship-change" -> effects.add(new EffectSpec(
                        "relationship-change", null, el.getAttribute("target"),
                        el.getAttribute("value"), null, null, null));
                case "dialogue" -> {
                    String speaker = el.getAttribute("speaker");
                    String lineRu = textContent(el, "line-ru");
                    String choice = el.hasAttribute("choice") ? el.getAttribute("choice") : null;
                    effects.add(new EffectSpec("dialogue", null, null, null, speaker, lineRu, choice));
                }
            }
        }
        return Collections.unmodifiableList(effects);
    }

    private String textContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }
}
