package ru.lifegame.assets.domain.model.docs;

import java.util.Collections;
import java.util.List;

/**
 * Result of the docs-preview generation use case.
 * Wraps the list of entity descriptors to be written to docs-preview.json.
 *
 * @param descriptors ordered list of entity docs descriptors (only abstract=false entries)
 */
public record DocsPreviewResult(List<EntityDocsDescriptor> descriptors) {
    public DocsPreviewResult {
        descriptors = descriptors != null ? Collections.unmodifiableList(descriptors) : List.of();
    }
}
