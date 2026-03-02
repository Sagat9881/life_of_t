package ru.lifegame.sprite.compositor;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

/**
 * Local PNG compositor for LPC character sprites.
 * Loads PNG layers from resources and composites them into a single sprite.
 */
@Slf4j
public class LPCSpriteCompositor {
    
    private static final int SPRITE_WIDTH = 832;  // 13 frames * 64px
    private static final int SPRITE_HEIGHT = 1344; // 21 rows * 64px
    
    private final Path spritesheetsPath;
    private final LayerConfigLoader configLoader;
    
    public LPCSpriteCompositor(Path spritesheetsPath) {
        this.spritesheetsPath = spritesheetsPath;
        this.configLoader = new LayerConfigLoader();
    }
    
    /**
     * Generate sprite from character configuration
     */
    public BufferedImage generateSprite(CharacterConfig config) throws IOException {
        log.info("Generating sprite for config: {}", config);
        
        // Load all required layers
        List<BufferedImage> layers = loadLayers(config);
        
        // Composite layers into final sprite
        return compositeLayers(layers);
    }
    
    /**
     * Load PNG layers based on character configuration
     */
    private List<BufferedImage> loadLayers(CharacterConfig config) throws IOException {
        List<BufferedImage> layers = new ArrayList<>();
        
        // Load body
        if (config.getBody() != null) {
            String bodyPath = String.format("body/%s/%s.png", 
                config.getGender(), config.getBody());
            layers.add(loadImage(bodyPath));
        }
        
        // Load hair
        if (config.getHair() != null) {
            String hairPath = String.format("hair/%s/%s.png",
                config.getHairStyle(), config.getHair());
            layers.add(loadImage(hairPath));
        }
        
        // Load clothes
        if (config.getClothes() != null) {
            for (String clothingItem : config.getClothes()) {
                String clothesPath = String.format("clothes/%s.png", clothingItem);
                layers.add(loadImage(clothesPath));
            }
        }
        
        return layers;
    }
    
    /**
     * Load image from spritesheets directory
     */
    private BufferedImage loadImage(String relativePath) throws IOException {
        Path fullPath = spritesheetsPath.resolve(relativePath);
        
        if (!Files.exists(fullPath)) {
            log.warn("Image not found: {}", fullPath);
            return createEmptyImage();
        }
        
        try (InputStream is = Files.newInputStream(fullPath)) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                log.error("Failed to load image: {}", fullPath);
                return createEmptyImage();
            }
            return image;
        }
    }
    
    /**
     * Composite layers into final sprite with alpha blending
     */
    private BufferedImage compositeLayers(List<BufferedImage> layers) {
        BufferedImage result = new BufferedImage(
            SPRITE_WIDTH, SPRITE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2d = result.createGraphics();
        g2d.setComposite(AlphaComposite.SrcOver);
        
        // Draw each layer on top of previous
        for (BufferedImage layer : layers) {
            g2d.drawImage(layer, 0, 0, null);
        }
        
        g2d.dispose();
        return result;
    }
    
    /**
     * Create empty transparent image as fallback
     */
    private BufferedImage createEmptyImage() {
        return new BufferedImage(
            SPRITE_WIDTH, SPRITE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
    }
    
    /**
     * Save generated sprite to file
     */
    public void saveSprite(BufferedImage sprite, Path outputPath) throws IOException {
        Files.createDirectories(outputPath.getParent());
        ImageIO.write(sprite, "PNG", outputPath.toFile());
        log.info("Sprite saved to: {}", outputPath);
    }
}