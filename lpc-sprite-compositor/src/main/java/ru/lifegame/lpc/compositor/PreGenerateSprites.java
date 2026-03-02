package ru.lifegame.lpc.compositor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Pre-generates sprites at build time from preset configurations
 */
@Slf4j
public class PreGenerateSprites {
    
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: PreGenerateSprites <presets.json> <output-dir>");
            System.exit(1);
        }
        
        String presetsFile = args[0];
        Path outputDir = Paths.get(args[1]);
        
        log.info("Generating sprites from presets: {}", presetsFile);
        log.info("Output directory: {}", outputDir);
        
        // Load presets
        ObjectMapper mapper = new ObjectMapper();
        SpritePresets presets;
        
        try (InputStream is = PreGenerateSprites.class.getClassLoader()
                .getResourceAsStream(presetsFile)) {
            if (is == null) {
                log.error("Presets file not found: {}", presetsFile);
                return;
            }
            presets = mapper.readValue(is, SpritePresets.class);
        }
        
        // Initialize compositor
        Path spritesheetsPath = Paths.get("lpc-spritesheets");
        LPCSpriteCompositor compositor = new LPCSpriteCompositor(spritesheetsPath);
        
        // Generate each preset
        int generated = 0;
        for (CharacterConfig config : presets.getCharacters()) {
            try {
                BufferedImage sprite = compositor.generateSprite(config);
                Path outputPath = outputDir.resolve(config.getId() + ".png");
                compositor.saveSprite(sprite, outputPath);
                generated++;
            } catch (IOException e) {
                log.error("Failed to generate sprite: {}", config.getId(), e);
            }
        }
        
        log.info("Successfully generated {} sprites", generated);
    }
    
    private static class SpritePresets {
        private List<CharacterConfig> characters;
        
        public List<CharacterConfig> getCharacters() {
            return characters;
        }
        
        public void setCharacters(List<CharacterConfig> characters) {
            this.characters = characters;
        }
    }
}