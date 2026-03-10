package ru.lifegame.backend.infrastructure.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.w3c.dom.*;
import ru.lifegame.backend.domain.conflict.spec.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Loads conflict specifications by scanning all XML files in
 * classpath*:game-config/conflicts/*.xml
 *
 * Uses Spring ResourcePatternResolver so it correctly scans
 * across all JARs on the classpath (including game-content module).
 *
 * Replaces the old single-file loadConflicts(path) approach.
 */
public class ConflictLoader implements ApplicationContextAware {

    private ResourcePatternResolver resolver;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        this.resolver = ResourcePatternUtils.getResourcePatternResolver(ctx);
    }

    /**
     * Scan and parse all conflict specs from all classpath JARs.
     */
    public List<ConflictSpec> loadAll() {
        try {
            Resource[] resources = resolver.getResources(
                    "classpath*:game-config/conflicts/*.xml"
            );
            if (resources.length == 0) {
                throw new IllegalStateException(
                        "No conflict specs found at classpath*:game-config/conflicts/*.xml"
                );
            }
            List<ConflictSpec> result = new ArrayList<>();
            for (Resource r : resources) {
                try (InputStream is = r.getInputStream()) {
                    result.add(parseConflictDocument(is));
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load conflict specs", e);
        }
    }

    private ConflictSpec parseConflictDocument(InputStream is) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);
            doc.getDocumentElement().normalize();
            return parseConflict(doc.getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse conflict XML", e);
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
        SuccessOutcome success = parseSuccessOutcome(successEl);
        FailureOutcome failure = parseFailureOutcome(failureEl);
        return new ConflictTacticSpec(code, label, description, baseCspCost, baseOpponentCspCost, success, failure);
    }

    private SuccessOutcome parseSuccessOutcome(Element outcomeEl) {
        if (outcomeEl == null) return new SuccessOutcome(Map.of(), Map.of(), null);
        return new SuccessOutcome(
                parseChanges(getChildElement(outcomeEl, "stat-changes")),
                parseChanges(getChildElement(outcomeEl, "relationship-changes")),
                getChildText(outcomeEl, "narrative")
        );
    }

    private FailureOutcome parseFailureOutcome(Element outcomeEl) {
        if (outcomeEl == null) return new FailureOutcome(Map.of(), Map.of(), null);
        return new FailureOutcome(
                parseChanges(getChildElement(outcomeEl, "stat-changes")),
                parseChanges(getChildElement(outcomeEl, "relationship-changes")),
                getChildText(outcomeEl, "narrative")
        );
    }

    private Map<String, Integer> parseChanges(Element changesEl) {
        if (changesEl == null) return Map.of();
        Map<String, Integer> changes = new HashMap<>();
        NodeList changeNodes = changesEl.getElementsByTagName("change");
        for (int i = 0; i < changeNodes.getLength(); i++) {
            Element changeEl = (Element) changeNodes.item(i);
            String field = changeEl.getAttribute("field");
            // Strip leading '+' so Integer.parseInt works for "+10"
            String rawValue = changeEl.getAttribute("value").replace("+", "");
            changes.put(field, Integer.parseInt(rawValue));
        }
        return changes;
    }

    private Element getChildElement(Element parent, String tagName) {
        if (parent == null) return null;
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    private String getChildText(Element parent, String tagName) {
        Element child = getChildElement(parent, tagName);
        return child != null ? child.getTextContent().trim() : null;
    }
}
