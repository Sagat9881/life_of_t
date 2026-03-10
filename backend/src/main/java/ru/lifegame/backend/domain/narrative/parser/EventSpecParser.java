package ru.lifegame.backend.domain.narrative.parser;

import ru.lifegame.backend.domain.npc.spec.EventSpec;
import ru.lifegame.backend.domain.npc.spec.EventSpec.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

/**
 * Parses a single narrative/events/*.xml file into an {@link EventSpec}.
 *
 * Expected XML structure (see EventSpec javadoc for full contract):
 * <pre>
 *   &lt;event id="..." type="RANDOM|TRIGGERED|SEASONAL"&gt;
 *     &lt;meta&gt;
 *       &lt;title-ru&gt;...&lt;/title-ru&gt;
 *       &lt;description-ru&gt;...&lt;/description-ru&gt;
 *       &lt;probability&gt;0.4&lt;/probability&gt;
 *       &lt;cooldown-hours&gt;48&lt;/cooldown-hours&gt;
 *     &lt;/meta&gt;
 *     &lt;conditions&gt;
 *       &lt;condition type="time_of_day" value="night"/&gt;
 *       &lt;condition type="stat_min" stat="anxiety" value="60"/&gt;
 *     &lt;/conditions&gt;
 *     &lt;dialogue&gt;
 *       &lt;line speaker="narrator" lang="ru"&gt;...&lt;/line&gt;
 *     &lt;/dialogue&gt;
 *     &lt;options&gt;
 *       &lt;option id="ok" label-ru="..."&gt;
 *         &lt;effects&gt;
 *           &lt;stat-change stat="happiness" value="+10"/&gt;
 *           &lt;relationship-change target="alexander" value="+2"/&gt;
 *         &lt;/effects&gt;
 *       &lt;/option&gt;
 *     &lt;/options&gt;
 *   &lt;/event&gt;
 * </pre>
 */
public class EventSpecParser {

    public EventSpec parse(File xmlFile) throws Exception {
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse event XML: " + xmlFile.getName(), e);
        }

        Element root = doc.getDocumentElement();
        String id   = require(root.getAttribute("id"),   xmlFile, "@id");
        String type = require(root.getAttribute("type"), xmlFile, "@type");

        EventMeta meta       = parseMeta(root, xmlFile, type);
        List<ConditionSpec> conditions = parseConditions(root);
        List<DialogueLine>  dialogue   = parseDialogue(root);
        List<OptionSpec>    options    = parseOptions(root, xmlFile);

        return new EventSpec(id, meta, conditions, dialogue, options);
    }

    // ── meta ────────────────────────────────────────────────────────────────

    private EventMeta parseMeta(Element root, File file, String type) throws Exception {
        NodeList metaNodes = root.getElementsByTagName("meta");
        if (metaNodes.getLength() == 0)
            throw new IllegalArgumentException("Missing <meta> in " + file.getName());

        Element meta = (Element) metaNodes.item(0);
        String titleRu      = text(meta, "title-ru");
        String descriptionRu = text(meta, "description-ru");
        double probability  = parseDouble(text(meta, "probability"), file, "probability");
        int cooldownHours   = parseInt(text(meta, "cooldown-hours"), file, "cooldown-hours");

        return new EventMeta(titleRu, descriptionRu, type, probability, cooldownHours);
    }

    // ── conditions ──────────────────────────────────────────────────────────

    private List<ConditionSpec> parseConditions(Element root) {
        List<ConditionSpec> result = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("condition");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            // only direct children of <conditions>
            if (!"conditions".equals(el.getParentNode().getNodeName())) continue;
            result.add(new ConditionSpec(
                el.getAttribute("type"),
                el.getAttribute("stat"),   // empty string if absent — that is fine
                el.getAttribute("value")
            ));
        }
        return result;
    }

    // ── dialogue ────────────────────────────────────────────────────────────

    private List<DialogueLine> parseDialogue(Element root) {
        List<DialogueLine> result = new ArrayList<>();
        NodeList dialogueNodes = root.getElementsByTagName("dialogue");
        if (dialogueNodes.getLength() == 0) return result;

        Element dialogueEl = (Element) dialogueNodes.item(0);
        NodeList lines = dialogueEl.getElementsByTagName("line");
        for (int i = 0; i < lines.getLength(); i++) {
            Element line = (Element) lines.item(i);
            result.add(new DialogueLine(
                line.getAttribute("speaker"),
                line.getTextContent().trim()
            ));
        }
        return result;
    }

    // ── options ─────────────────────────────────────────────────────────────

    private List<OptionSpec> parseOptions(Element root, File file) throws Exception {
        NodeList optionsNodes = root.getElementsByTagName("options");
        if (optionsNodes.getLength() == 0)
            throw new IllegalArgumentException("Missing <options> in " + file.getName() +
                ". Every event must have at least one option.");

        Element optionsEl = (Element) optionsNodes.item(0);
        NodeList optionNodes = optionsEl.getElementsByTagName("option");
        if (optionNodes.getLength() == 0)
            throw new IllegalArgumentException("<options> has no <option> children in " + file.getName());

        List<OptionSpec> options = new ArrayList<>();
        for (int i = 0; i < optionNodes.getLength(); i++) {
            Element opt = (Element) optionNodes.item(i);
            String optId      = require(opt.getAttribute("id"),       file, "option/@id");
            String labelRu    = require(opt.getAttribute("label-ru"), file, "option/@label-ru");
            List<EventSpec.EffectSpec> effects = parseEffects(opt);
            options.add(new OptionSpec(optId, labelRu, effects));
        }
        return options;
    }

    private List<EventSpec.EffectSpec> parseEffects(Element optionEl) {
        List<EventSpec.EffectSpec> result = new ArrayList<>();
        NodeList effectsNodes = optionEl.getElementsByTagName("effects");
        if (effectsNodes.getLength() == 0) return result;

        Element effectsEl = (Element) effectsNodes.item(0);

        // stat-change
        NodeList statChanges = effectsEl.getElementsByTagName("stat-change");
        for (int i = 0; i < statChanges.getLength(); i++) {
            Element el = (Element) statChanges.item(i);
            result.add(new EventSpec.EffectSpec("stat-change", el.getAttribute("stat"), el.getAttribute("value")));
        }

        // relationship-change
        NodeList relChanges = effectsEl.getElementsByTagName("relationship-change");
        for (int i = 0; i < relChanges.getLength(); i++) {
            Element el = (Element) relChanges.item(i);
            result.add(new EventSpec.EffectSpec("relationship-change", el.getAttribute("target"), el.getAttribute("value")));
        }

        return result;
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private String text(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private String require(String value, File file, String field) throws Exception {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Missing required field '" + field + "' in " + file.getName());
        return value;
    }

    private double parseDouble(String value, File file, String field) throws Exception {
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid double for '" + field + "' in " + file.getName() + ": " + value);
        }
    }

    private int parseInt(String value, File file, String field) throws Exception {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid int for '" + field + "' in " + file.getName() + ": " + value);
        }
    }
}
