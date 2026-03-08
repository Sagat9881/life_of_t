package ru.lifegame.backend.infrastructure.narrative;

import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.spec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.ScoredAction;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class NarrativeContentLoader {

    private final List<NpcSpec> npcSpecs = new ArrayList<>();

    public void loadFromDirectory(Path narrativeRoot) throws Exception {
        Path npcDir = narrativeRoot.resolve("npc-behavior");
        if (!Files.exists(npcDir)) return;
        try (Stream<Path> files = Files.list(npcDir)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".xml")).toList()) {
                try (InputStream is = Files.newInputStream(file)) {
                    npcSpecs.add(parseNpc(is));
                }
            }
        }
    }

    private NpcSpec parseNpc(InputStream xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(xml);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        String type = root.hasAttribute("type") ? root.getAttribute("type") : "named";
        String category = root.hasAttribute("category") ? root.getAttribute("category") : "human";
        String displayName = getTextContent(root, "display-name");

        Map<String, Integer> personality = new HashMap<>();
        NodeList traitNodes = root.getElementsByTagName("trait");
        for (int i = 0; i < traitNodes.getLength(); i++) {
            Element el = (Element) traitNodes.item(i);
            personality.put(el.getAttribute("name"), Integer.parseInt(el.getAttribute("value")));
        }

        Map<String, Integer> moodInitial = new HashMap<>();
        NodeList moodNodes = root.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            NamedNodeMap attrs = moodNodes.item(0).getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                moodInitial.put(attr.getName(), Integer.parseInt(attr.getValue()));
            }
        }

        List<ScheduleSlot> schedule = new ArrayList<>();
        NodeList slotNodes = root.getElementsByTagName("slot");
        for (int i = 0; i < slotNodes.getLength(); i++) {
            Element el = (Element) slotNodes.item(i);
            schedule.add(new ScheduleSlot(
                Integer.parseInt(el.getAttribute("start")),
                Integer.parseInt(el.getAttribute("end")),
                el.getAttribute("activity"),
                el.getAttribute("location"),
                el.getAttribute("animation")
            ));
        }

        List<ScoredAction> actions = new ArrayList<>();

        return new NpcSpec(id, type, category, displayName, personality, moodInitial, true, 10, schedule, actions);
    }

    private String getTextContent(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    public List<NpcSpec> npcSpecs() { return Collections.unmodifiableList(npcSpecs); }
}
