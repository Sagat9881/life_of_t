package ru.lifegame.assets.application.usecase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.infrastructure.parser.XmlAssetSpecParser;

import java.nio.file.Path;

/**
 * Use case: parse any existing XML prompt file and convert it
 * into the unified AssetSpec format.
 */
public class UnifyXmlSpecsUseCase {

    private static final Logger log = LoggerFactory.getLogger(UnifyXmlSpecsUseCase.class);

    private final XmlAssetSpecParser parser;

    public UnifyXmlSpecsUseCase(XmlAssetSpecParser parser) {
        this.parser = parser;
    }

    /**
     * Parses the given XML file and returns a unified AssetSpec.
     *
     * @param xmlFile path to the XML specification file
     * @return parsed AssetSpec in unified format
     */
    public AssetSpec execute(Path xmlFile) {
        log.info("Unifying XML spec: {}", xmlFile);
        return parser.parse(xmlFile);
    }
}
