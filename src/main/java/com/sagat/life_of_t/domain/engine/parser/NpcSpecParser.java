package com.sagat.life_of_t.domain.engine.parser;

import com.sagat.life_of_t.domain.engine.spec.NpcSpec;
import com.sagat.life_of_t.domain.engine.spec.NpcSpec.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public class NpcSpecParser {

    public NpcSpec parse(InputStream xml) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();
        String entityId = root.getAttribute("entity-id");

        Map<String, Integer> traits = parsePersonality(root);
        List<ScheduleSlot> schedules = parseSchedules(root);
        List<ReactionSpec> reactions = parseReactions(root);

        boolean hasMemory = !reactions.isEmpty() && traits.size() > 3;
        NpcType type = hasMemory ? NpcType.NAMED : NpcType.FILLER;

        return new NpcSpec(entityId, type, traits, schedules, reactions);
    }

    private Map<String, Integer> parsePersonality(Element root) {
        Map<String, Integer> traits = new LinkedHashMap<>();
        NodeList nodes = root.getElementsByTagName("trait");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            traits.put(el.getAttribute("name"), Integer.parseInt(el.getAttribute("value")));
        }
        return Collections.unmodifiableMap(traits);
    }

    private List<ScheduleSlot> parseSchedules(Element root) {
        List<ScheduleSlot> slots = new ArrayList<>();
        NodeList scheduleNodes = root.getElementsByTagName("schedule");
        for (int i = 0; i < scheduleNodes.getLength(); i++) {
            Element sched = (Element) scheduleNodes.item(i);
            String time = sched.getAttribute("time");
            List<ActionSpec> actions = parseActions(sched);
            slots.add(new ScheduleSlot(time, actions));
        }
        return Collections.unmodifiableList(slots);
    }

    private List<ActionSpec> parseActions(Element parent) {
        List<ActionSpec> actions = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element el)) continue;
            if (!"action".equals(el.getTagName())) continue;

            String type = el.getAttribute("type");
            String target = el.hasAttribute("target") ? el.getAttribute("target") : null;
            String value = el.hasAttribute("value") ? el.getAttribute("value") : null;
            double prob = el.hasAttribute("probability")
                    ? Double.parseDouble(el.getAttribute("probability")) : 1.0;

            String lineRu = null;
            NodeList lines = el.getElementsByTagName("line-ru");
            if (lines.getLength() > 0) lineRu = lines.item(0).getTextContent().trim();

            actions.add(new ActionSpec(type, target, value, prob, lineRu));
        }
        return Collections.unmodifiableList(actions);
    }

    private List<ReactionSpec> parseReactions(Element root) {
        List<ReactionSpec> reactions = new ArrayList<>();
        NodeList reactionNodes = root.getElementsByTagName("reaction");
        for (int i = 0; i < reactionNodes.getLength(); i++) {
            Element el = (Element) reactionNodes.item(i);
            String trigger = el.getAttribute("trigger");
            Map<String, String> attrs = new LinkedHashMap<>();
            NamedNodeMap attrMap = el.getAttributes();
            for (int j = 0; j < attrMap.getLength(); j++) {
                Attr a = (Attr) attrMap.item(j);
                if (!"trigger".equals(a.getName())) attrs.put(a.getName(), a.getValue());
            }
            List<ActionSpec> actions = parseActions(el);
            reactions.add(new ReactionSpec(trigger, Collections.unmodifiableMap(attrs), actions));
        }
        return Collections.unmodifiableList(reactions);
    }
}
