package ru.lifegame.backend.infrastructure.narrative;

import ru.lifegame.backend.domain.npc.spec.NpcSpec;
import ru.lifegame.backend.domain.npc.spec.ScheduleSlot;
import ru.lifegame.backend.domain.npc.spec.ScoredAction;
import ru.lifegame.backend.domain.npc.spec.ConditionSpec;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class NarrativeContentLoader {

    private final List<NpcSpec> npcSpecs = new ArrayList<>();

    public void loadNpcSpecs(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] xmlFiles = dir.listFiles((d, name) -> name.endsWith(".xml"));
        if (xmlFiles == null) return;

        for (File file : xmlFiles) {
            try {
                NpcSpec spec = parseNpcFile(file);
                if (spec != null) npcSpecs.add(spec);
            } catch (Exception e) {
                System.err.println("Failed to parse NPC spec: " + file.getName() + " - " + e.getMessage());
            }
        }
    }

    private NpcSpec parseNpcFile(File file) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        Element root = doc.getDocumentElement();

        String id = root.getAttribute("id");
        String type = root.getAttribute("type");
        String category = root.getAttribute("category");
        String displayName = getTextContent(root, "display-name");

        Map<String, Integer> personality = new HashMap<>();
        NodeList traitNodes = root.getElementsByTagName("trait");
        for (int i = 0; i < traitNodes.getLength(); i++) {
            Element el = (Element) traitNodes.item(i);
            if ("personality".equals(((Element) el.getParentNode()).getTagName())) {
                personality.put(el.getAttribute("name"), Integer.parseInt(el.getAttribute("value")));
            }
        }

        Map<String, Integer> moodInitial = new HashMap<>();
        NodeList moodNodes = root.getElementsByTagName("mood-initial");
        if (moodNodes.getLength() > 0) {
            NamedNodeMap attrs = ((Element) moodNodes.item(0)).getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                Attr attr = (Attr) attrs.item(i);
                moodInitial.put(attr.getName(), Integer.parseInt(attr.getValue()));
            }
        }

        boolean memoryEnabled = "true".equals(getAttrFromChild(root, "memory", "enabled"));
        int shortTermSize = parseIntFromChild(root, "memory", "short-term-size", 10);

        List<ScheduleSlot> schedule = new ArrayList<>();
        NodeList slotNodes = root.getElementsByTagName("slot");
        for (int i = 0; i < slotNodes.getLength(); i++) {
            Element el = (Element) slotNodes.item(i);
            if ("schedule".equals(((Element) el.getParentNode()).getTagName())) {
                schedule.add(new ScheduleSlot(
                    Integer.parseInt(el.getAttribute("start")),
                    Integer.parseInt(el.getAttribute("end")),
                    el.getAttribute("activity"),
                    el.getAttribute("location"),
                    el.getAttribute("animation")
                ));
            }
        }

        List<ScoredAction> actions = new ArrayList<>();
        List<String> questLines = List.of();
        String ql = getTextContent(root, "quest-lines");
        if (!ql.isBlank()) {
            questLines = Arrays.stream(ql.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }

        return new NpcSpec(id, type, category, displayName, personality, moodInitial,
            memoryEnabled, shortTermSize, schedule, actions, questLines);
    }

    private String getTextContent(Element root, String tag) {
        NodeList n = root.getElementsByTagName(tag);
        return n.getLength() > 0 ? n.item(0).getTextContent().trim() : "";
    }

    private String getAttrFromChild(Element root, String childTag, String attr) {
        NodeList n = root.getElementsByTagName(childTag);
        return n.getLength() > 0 ? ((Element) n.item(0)).getAttribute(attr) : "";
    }

    private int parseIntFromChild(Element root, String childTag, String attr, int def) {
        String v = getAttrFromChild(root, childTag, attr);
        return v.isEmpty() ? def : Integer.parseInt(v);
    }

    public List<NpcSpec> npcSpecs() { return npcSpecs; }
}
