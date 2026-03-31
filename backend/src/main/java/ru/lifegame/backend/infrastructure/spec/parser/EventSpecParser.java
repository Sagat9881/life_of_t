package ru.lifegame.backend.infrastructure.spec.parser;

import ru.lifegame.backend.domain.narrative.spec.EventSpec;
import ru.lifegame.backend.domain.narrative.spec.EventSpec.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * Parses a single narrative/events/*.xml file into an {@link EventSpec}.
 *
 * <p>Moved from {@code domain/narrative/parser/} to {@code infrastructure/spec/parser/}
 * as part of TASK-BE-026. XML/DOM parsing is an infrastructure concern
 * (java-developer-skill.md §7).
 *
 * <p>The class in {@code domain/narrative/parser/EventSpecParser} is now
 * {@code @Deprecated} and delegates here.
 */
public class EventSpecParser {

    public EventSpec parse(InputStream xmlStream, String filename) throws Exception {
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse event XML: " + filename, e);
        }
        return parseDocument(doc, filename);
    }

    public EventSpec parse(File xmlFile) throws Exception {
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse event XML: " + xmlFile.getName(), e);
        }
        return parseDocument(doc, xmlFile.getName());
    }

    private EventSpec parseDocument(Document doc, String filename) throws Exception {
        Element root = doc.getDocumentElement();
        String id   = require(root.getAttribute("id"),   filename, "@id");
        String type = require(root.getAttribute("type"), filename, "@type");
        EventMeta           meta       = parseMeta(root, filename, type);
        List<ConditionSpec> conditions = parseConditions(root);
        List<DialogueLine>  dialogue   = parseDialogue(root);
        List<OptionSpec>    options    = parseOptions(root, filename);
        return new EventSpec(id, meta, conditions, dialogue, options);
    }

    private EventMeta parseMeta(Element root, String filename, String type) throws Exception {
        NodeList metaNodes = root.getElementsByTagName("meta");
        if (metaNodes.getLength() == 0)
            throw new IllegalArgumentException("Missing <meta> in " + filename);
        Element meta = (Element) metaNodes.item(0);
        return new EventMeta(
                text(meta, "title-ru"),
                text(meta, "description-ru"),
                type,
                parseDouble(text(meta, "probability"),   filename, "probability"),
                parseInt  (text(meta, "cooldown-hours"), filename, "cooldown-hours")
        );
    }

    private List<ConditionSpec> parseConditions(Element root) {
        List<ConditionSpec> result = new ArrayList<>();
        NodeList nodes = root.getElementsByTagName("condition");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (!"conditions".equals(el.getParentNode().getNodeName())) continue;
            result.add(new ConditionSpec(el.getAttribute("type"), el.getAttribute("stat"), el.getAttribute("value")));
        }
        return result;
    }

    private List<DialogueLine> parseDialogue(Element root) {
        List<DialogueLine> result = new ArrayList<>();
        NodeList dialogueNodes = root.getElementsByTagName("dialogue");
        if (dialogueNodes.getLength() == 0) return result;
        NodeList lines = ((Element) dialogueNodes.item(0)).getElementsByTagName("line");
        for (int i = 0; i < lines.getLength(); i++) {
            Element line = (Element) lines.item(i);
            result.add(new DialogueLine(line.getAttribute("speaker"), line.getTextContent().trim()));
        }
        return result;
    }

    private List<OptionSpec> parseOptions(Element root, String filename) throws Exception {
        NodeList optionsNodes = root.getElementsByTagName("options");
        if (optionsNodes.getLength() == 0)
            throw new IllegalArgumentException("Missing <options> in " + filename);
        Element optionsEl = (Element) optionsNodes.item(0);
        NodeList optionNodes = optionsEl.getElementsByTagName("option");
        if (optionNodes.getLength() == 0)
            throw new IllegalArgumentException("<options> has no <option> children in " + filename);
        List<OptionSpec> options = new ArrayList<>();
        for (int i = 0; i < optionNodes.getLength(); i++) {
            Element opt = (Element) optionNodes.item(i);
            options.add(new OptionSpec(
                    require(opt.getAttribute("id"),       filename, "option/@id"),
                    require(opt.getAttribute("label-ru"), filename, "option/@label-ru"),
                    parseEffects(opt)
            ));
        }
        return options;
    }

    private List<EffectSpec> parseEffects(Element optionEl) {
        List<EffectSpec> result = new ArrayList<>();
        NodeList effectsNodes = optionEl.getElementsByTagName("effects");
        if (effectsNodes.getLength() == 0) return result;
        Element effectsEl = (Element) effectsNodes.item(0);
        NodeList statChanges = effectsEl.getElementsByTagName("stat-change");
        for (int i = 0; i < statChanges.getLength(); i++) {
            Element el = (Element) statChanges.item(i);
            result.add(new EffectSpec("stat-change", el.getAttribute("stat"), el.getAttribute("value")));
        }
        NodeList relChanges = effectsEl.getElementsByTagName("relationship-change");
        for (int i = 0; i < relChanges.getLength(); i++) {
            Element el = (Element) relChanges.item(i);
            result.add(new EffectSpec("relationship-change", el.getAttribute("target"), el.getAttribute("value")));
        }
        return result;
    }

    private String text(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? nodes.item(0).getTextContent().trim() : "";
    }

    private String require(String value, String filename, String field) throws Exception {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Missing required field '" + field + "' in " + filename);
        return value;
    }

    private double parseDouble(String value, String filename, String field) throws Exception {
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid double for '" + field + "' in " + filename + ": " + value);
        }
    }

    private int parseInt(String value, String filename, String field) throws Exception {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid int for '" + field + "' in " + filename + ": " + value);
        }
    }
}
