package ru.lifegame.backend.domain.ending.spec;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EndingLoader {

    public List<EndingSpec> load(InputStream xmlStream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlStream);
        doc.getDocumentElement().normalize();

        List<EndingSpec> specs = new ArrayList<>();
        NodeList endingNodes = doc.getElementsByTagName("ending");

        for (int i = 0; i < endingNodes.getLength(); i++) {
            Element endingEl = (Element) endingNodes.item(i);
            specs.add(parseEnding(endingEl));
        }

        return specs;
    }

    private EndingSpec parseEnding(Element endingEl) {
        String id = endingEl.getAttribute("id");
        String category = endingEl.getAttribute("category");
        int priority = Integer.parseInt(endingEl.getAttribute("priority"));

        Element metaEl = (Element) endingEl.getElementsByTagName("meta").item(0);
        EndingSpec.EndingMeta meta = parseMeta(metaEl);

        Element conditionsEl = (Element) endingEl.getElementsByTagName("conditions").item(0);
        EndingSpec.EndingConditions conditions = parseConditions(conditionsEl);

        return new EndingSpec(id, category, priority, meta, conditions);
    }

    private EndingSpec.EndingMeta parseMeta(Element metaEl) {
        String title = getTextContent(metaEl, "title");
        String summary = getTextContent(metaEl, "summary");
        String epilogue = getTextContent(metaEl, "epilogue");
        return new EndingSpec.EndingMeta(title, summary, epilogue);
    }

    private EndingSpec.EndingConditions parseConditions(Element conditionsEl) {
        String mode = conditionsEl.getAttribute("mode");
        if (mode == null || mode.isEmpty()) mode = "AND";

        List<EndingSpec.EndingCondition> conditions = new ArrayList<>();
        NodeList conditionNodes = conditionsEl.getElementsByTagName("condition");

        for (int i = 0; i < conditionNodes.getLength(); i++) {
            Element condEl = (Element) conditionNodes.item(i);
            conditions.add(parseCondition(condEl));
        }

        return new EndingSpec.EndingConditions(mode, conditions);
    }

    private EndingSpec.EndingCondition parseCondition(Element condEl) {
        String type = condEl.getAttribute("type");
        String field = condEl.getAttribute("field");
        String target = condEl.getAttribute("target");
        String operator = condEl.getAttribute("operator");
        String valueStr = condEl.getAttribute("value");
        Double value = (valueStr != null && !valueStr.isEmpty()) ? Double.parseDouble(valueStr) : null;
        String questId = condEl.getAttribute("questId");
        String state = condEl.getAttribute("state");

        return new EndingSpec.EndingCondition(type, field, target, operator, value, questId, state);
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }
}
