package ru.lifegame.assets.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.lifegame.assets.AssetRequest;
import ru.lifegame.assets.AssetType;
import ru.lifegame.assets.GeneratorRegistry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Generates all game assets when the application is started with the
 * {@code --generate-assets} command-line argument.
 */
@Component
public class AssetGenerationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AssetGenerationRunner.class);

    private final GeneratorRegistry registry;

    @Value("${asset.generator.output-dir:./generated-assets}")
    private String outputDir;

    public AssetGenerationRunner(GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void run(String... args) throws Exception {
        boolean shouldGenerate = Arrays.stream(args)
                .anyMatch(a -> "--generate-assets".equalsIgnoreCase(a));

        if (!shouldGenerate) {
            log.info("Asset generator started. Use --generate-assets to produce output files.");
            return;
        }

        File outDir = new File(outputDir);
        outDir.mkdirs();
        log.info("Generating assets into: {}", outDir.getAbsolutePath());

        generateTextures(outDir);
        generateCharacters(outDir);

        log.info("Asset generation complete.");
    }

    private void generateTextures(File outDir) {
        List<TextureSpec> specs = List.of(
                new TextureSpec("wood_floor",  "wood",      Map.of("symmetry", "quad", "fill_probability", "0.45", "seed", "42")),
                new TextureSpec("stone_wall",  "stone",     Map.of("symmetry", "quad", "fill_probability", "0.50", "seed", "7")),
                new TextureSpec("carpet",      "fabric",    Map.of("symmetry", "vertical", "fill_probability", "0.35", "seed", "99")),
                new TextureSpec("wallpaper",   "wallpaper", Map.of("symmetry", "quad", "fill_probability", "0.30", "seed", "123"))
        );

        for (TextureSpec spec : specs) {
            Map<String, String> params = new java.util.HashMap<>(spec.params());
            params.put("palette", spec.palette());

            AssetRequest request = new AssetRequest(
                    AssetType.TEXTURE, spec.name(), 32, 32, params);

            BufferedImage image = registry.generate(request);
            save(image, new File(outDir, "textures/" + spec.name() + ".png"));
        }
    }

    private record TextureSpec(String name, String palette, Map<String, String> params) {}

    private void generateCharacters(File outDir) {
        List<String> characters = List.of("tatyana", "husband");

        for (String character : characters) {
            AssetRequest request = new AssetRequest(
                    AssetType.CHARACTER, character, 64, 64,
                    Map.of("character", character));

            BufferedImage image = registry.generate(request);
            save(image, new File(outDir, "characters/" + character + ".png"));
        }
    }

    private void save(BufferedImage image, File file) {
        try {
            file.getParentFile().mkdirs();
            ImageIO.write(image, "PNG", file);
            log.info("  Saved: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("  Failed to save {}: {}", file.getAbsolutePath(), e.getMessage(), e);
        }
    }
}
