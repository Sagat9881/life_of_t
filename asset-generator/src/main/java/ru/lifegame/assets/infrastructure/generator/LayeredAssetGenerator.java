package ru.lifegame.assets.infrastructure.generator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lifegame.assets.domain.model.asset.AnimationSpec;
import ru.lifegame.assets.domain.model.asset.AssetLayer;
import ru.lifegame.assets.domain.model.asset.AssetSpec;
import ru.lifegame.assets.domain.service.AssetGenerationService;
import ru.lifegame.assets.infrastructure.writer.AtlasConfigWriter;
import ru.lifegame.assets.infrastructure.writer.PngLayerWriter;
import ru.lifegame.assets.infrastructure.writer.WebpAtlasWriter;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates layered PNG assets and animation atlases from XML-driven AssetSpec.
 * Delegates all pixel rendering to UniversalPixelRenderer.
 */
public class LayeredAssetGenerator implements AssetGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LayeredAssetGenerator.class);
    private static final int DEFAULT_WIDTH = 128;
    private static final int DEFAULT_HEIGHT = 128;

    private final UniversalPixelRenderer renderer;
    private final PngLayerWriter pngWriter;
    private final WebpAtlasWriter atlasWriter;
    private final AtlasConfigWriter configWriter;

    public LayeredAssetGenerator(UniversalPixelRenderer renderer,
                                 PngLayerWriter pngWriter,
                                 WebpAtlasWriter atlasWriter,
                                 AtlasConfigWriter configWriter) {
        this.renderer = renderer;
        this.pngWriter = pngWriter;
        this.atlasWriter = atlasWriter;
        this.configWriter = configWriter;
    }

    @Override
    public List<Path> generateAsset(AssetSpec spec, Path outputRoot) {
        List<Path> generated = new ArrayList<>();
        Path entityDir = outputRoot.resolve(spec.naming().outputDir());

        // 1. Generate composite static image from all layers
        int width = resolveWidth(spec);
        int height = resolveHeight(spec);
        BufferedImage composite = renderer.renderComposite(spec.layers(), width, height);
        try {
            Path compositePath = pngWriter.writeToPath(composite,
                    entityDir.resolve(spec.entityName() + ".png"));
            generated.add(compositePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write composite: " + spec.entityName(), e);
        }

        // 2. Generate individual layers
        for (AssetLayer layer : spec.layers()) {
            BufferedImage layerImage = renderer.renderLayer(layer, width, height);
            try {
                Path written = pngWriter.write(layerImage, layer, entityDir);
                generated.add(written);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write layer: " + layer.id(), e);
            }
        }

        // 3. Generate animation atlases
        if (!spec.animations().isEmpty()) {
            Path animDir = entityDir.resolve("animations");
            for (AnimationSpec animSpec : spec.animations()) {
                List<BufferedImage> frames = renderer.renderAnimationFrames(
                        spec.layers(), animSpec);
                try {
                    Path atlasPath = atlasWriter.writeAtlas(frames, animSpec, animDir);
                    generated.add(atlasPath);

                    String atlasFileName = atlasPath.getFileName().toString();
                    Path configPath = configWriter.writeConfig(animSpec, atlasFileName, animDir);
                    generated.add(configPath);
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to write atlas: " + animSpec.name(), e);
                }
            }
            try {
                Path combinedConfig = configWriter.writeCombinedConfig(spec.animations(), animDir);
                generated.add(combinedConfig);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to write combined atlas config", e);
            }
        }

        log.info("Generated {} files for {}/{}", generated.size(),
                spec.entityType(), spec.entityName());
        return generated;
    }

    private int resolveWidth(AssetSpec spec) {
        return spec.layers().stream()
                .mapToInt(AssetLayer::width)
                .filter(w -> w > 0)
                .max()
                .orElse(DEFAULT_WIDTH);
    }

    private int resolveHeight(AssetSpec spec) {
        return spec.layers().stream()
                .mapToInt(AssetLayer::height)
                .filter(h -> h > 0)
                .max()
                .orElse(DEFAULT_HEIGHT);
    }
}
