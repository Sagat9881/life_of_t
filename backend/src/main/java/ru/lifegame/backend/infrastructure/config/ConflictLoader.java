package ru.lifegame.backend.infrastructure.config;

import org.w3c.dom.*;
import ru.lifegame.backend.domain.conflict.spec.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

/**
 * Loads conflict specifications from conflicts.xml.
 * Replaces hardcoded ConflictType enum and trigger classes.
 */
public class ConflictLoader {

    public List<ConflictSpec> loadConflicts(String resourcePath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Conflicts config not found: " + resourcePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();

            NodeList conflictNodes = doc.getElementsByTagName("conflict");
            List<ConflictSpec> conflicts = new ArrayList<>();

            for (int i = 0; i < conflictNodes.getLength(); i++) {
                Element conflictEl = (Element) conflictNodes.item(i);
                conflicts.add(parseConflict(conflictEl));
            }

            return conflicts;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load conflicts.xml", e);
        }
    }

    private ConflictSpec parseConflict(Element conflictEl) {
        String id = conflictEl.getAttribute("id");

        ConflictMeta meta = parseMeta(getChildElement(conflictEl, "meta"));
        ConflictTriggerSpec trigger = parseTrigger(getChildElement(conflictEl, "trigger"));
        List<ConflictTacticSpec> tactics = parseTactics(conflictEl);

        return new ConflictSpec(id, meta, trigger, tactics);
    }

    private ConflictMeta parseMeta(Element metaEl) {
        return new ConflictMeta(
                getChildText(metaEl, "label"),
                getChildText(metaEl, "description"),
                getChildText(metaEl, "opponent-id"),
                getChildText(metaEl, "category")
        );
    }

    private ConflictTriggerSpec parseTrigger(Element triggerEl) {
        List<TriggerCondition> conditions = new ArrayList<>();
        NodeList conditionNodes = triggerEl.getElementsByTagName("condition");

        for (int i = 0; i < conditionNodes.getLength(); i++) {
            Element condEl = (Element) conditionNodes.item(i);
            conditions.add(parseCondition(condEl));
        }

        int cooldown = Integer.parseInt(triggerEl.getAttribute("cooldown-days"));
        String mode = triggerEl.getAttribute("mode");
        if (mode == null || mode.isEmpty()) mode = "ALL";

        return new ConflictTriggerSpec(conditions, cooldown, mode);
    }

    private TriggerCondition parseCondition(Element condEl) {
        String type = condEl.getAttribute("type");
        String field = condEl.getAttribute("field");
        String operator = condEl.getAttribute("operator");
        String valueStr = condEl.getAttribute("value");
        Double value = (valueStr != null && !valueStr.isEmpty()) ? Double.parseDouble(valueStr) : null;
        String target = condEl.getAttribute("target");
        String questId = condEl.getAttribute("quest-id");
        String questState = condEl.getAttribute("quest-state");

        return new TriggerCondition(type, field, operator, value, target, questId, questState, Map.of());
    }

    private List<ConflictTacticSpec> parseTactics(Element conflictEl) {
        List<ConflictTacticSpec> tactics = new ArrayList<>();
        NodeList tacticNodes = conflictEl.getElementsByTagName("tactic");

        for (int i = 0; i < tacticNodes.getLength(); i++) {
            Element tacticEl = (Element) tacticNodes.item(i);
            tactics.add(parseTactic(tacticEl));
        }

        return tactics;
    }

    private ConflictTacticSpec parseTactic(Element tacticEl) {
        String code = tacticEl.getAttribute("code");
        String label = getChildText(tacticEl, "label");
        String description = getChildText(tacticEl, "description");
        int baseCspCost = Integer.parseInt(tacticEl.getAttribute("base-csp-cost"));
        int baseOpponentCspCost = Integer.parseInt(tacticEl.getAttribute("base-opponent-csp-cost"));

        Element successEl = getChildElement(tacticEl, "success-outcome");
        Element failureEl = getChildElement(tacticEl, "failure-outcome");

        SuccessOutcome success = parseOutcome(successEl, SuccessOutcome.class);
        FailureOutcome failure = parseOutcome(failureEl, FailureOutcome.class);

        return new ConflictTacticSpec(code, label, description, baseCspCost, baseOpponentCspCost, success, failure);
    }

    private <T> T parseOutcome(Element outcomeEl, Class<T> outcomeClass) {
        if (outcomeEl == null) return null;

        Map<String, Integer> statChanges = parseChanges(getChildElement(outcomeEl, "stat-changes"));
        Map<String, Integer> relChanges = parseChanges(getChildElement(outcomeEl, "relationship-changes"));
        String narrative = getChildText(outcomeEl, "narrative");

        try {
            return outcomeClass.getConstructor(Map.class, Map.class, String.class)
                    .newInstance(statChanges, relChanges, narrative);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create outcome", e);
        }
    }

    private Map<String, Integer> parseChanges(Element changesEl) {
        if (changesEl == null) return Map.of();

        Map<String, Integer> changes = new HashMap<>();
        NodeList changeNodes = changesEl.getElementsByTagName("change");

        for (int i = 0; i < changeNodes.getLength(); i++) {
            Element changeEl = (Element) changeNodes.item(i);
            String field = changeEl.getAttribute("field");
            int value = Integer.parseInt(changeEl.getAttribute("value"));
            changes.put(field, value);
        }

        return changes;
    }

    private Element getChildElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private String getChildText(Element parent, String tagName) {
        Element child = getChildElement(parent, tagName);
        return child != null ? child.getTextContent().trim() : null;
    }
}
