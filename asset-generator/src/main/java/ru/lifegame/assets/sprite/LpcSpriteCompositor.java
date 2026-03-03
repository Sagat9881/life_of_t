package ru.lifegame.assets.sprite;

import ru.lifegame.assets.AssetGenerator;
import ru.lifegame.assets.AssetRequest;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LpcSpriteCompositor implements AssetGenerator {

    private static final Map<SpriteLayerCategory, Color> PLACEHOLDER_COLORS = Map.of(
            SpriteLayerCategory.BASE_BODY,        new Color(0xFFD39B),
            SpriteLayerCategory.SHOES,            new Color(0xFFFFFF),
            SpriteLayerCategory.CLOTHING_BOTTOM,  new Color(0x4A90D9),
            SpriteLayerCategory.CLOTHING_TOP,     new Color(0x7EC8E3),
            SpriteLayerCategory.EYES,             new Color(0x3B82C4),
            SpriteLayerCategory.HAIR,             new Color(0x8B5E3C),
            SpriteLayerCategory.ACCESSORY,        new Color(0x444444)
    );

    private final String lpcBasePath;

    public LpcSpriteCompositor() {
        this("");
    }

    public LpcSpriteCompositor(String lpcBasePath) {
        this.lpcBasePath = lpcBasePath != null ? lpcBasePath : "";
    }

    @Override
    public String name() {
        return "LpcSpriteCompositor";
    }

    @Override
    public BufferedImage generate(AssetRequest request) {
        String characterName = request.param("character", "tatyana");
        CharacterDefinition definition = resolveCharacter(characterName);
        return composite(definition, request.width(), request.height());
    }

    public BufferedImage composite(CharacterDefinition definition, int width, int height) {
        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        List<SpriteLayer> sorted = definition.layers().stream()
                .sorted(Comparator.comparingInt(SpriteLayer::zOrder))
                .toList();

        for (SpriteLayer layer : sorted) {
            BufferedImage layerImage = loadLayerImage(layer, width, height);
            g.drawImage(layerImage, 0, 0, width, height, null);
        }

        g.dispose();
        return canvas;
    }

    BufferedImage loadLayerImage(SpriteLayer layer, int width, int height) {
        if (layer.imagePath() != null && !layer.imagePath().isBlank()) {
            BufferedImage fromFile = tryLoadFile(layer.imagePath());
            if (fromFile != null) return fromFile;

            if (!lpcBasePath.isBlank()) {
                fromFile = tryLoadFile(lpcBasePath + File.separator + layer.imagePath());
                if (fromFile != null) return fromFile;
            }

            BufferedImage fromClasspath = tryLoadClasspath(layer.imagePath());
            if (fromClasspath != null) return fromClasspath;
        }

        return generatePlaceholder(layer, width, height);
    }

    private BufferedImage tryLoadFile(String path) {
        try {
            File f = new File(path);
            if (f.exists() && f.isFile()) {
                return ImageIO.read(f);
            }
        } catch (IOException ignored) {}
        return null;
    }

    private BufferedImage tryLoadClasspath(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is != null) return ImageIO.read(is);
        } catch (IOException ignored) {}
        return null;
    }

    BufferedImage generatePlaceholder(SpriteLayer layer, int width, int height) {
        Color base = PLACEHOLDER_COLORS.getOrDefault(layer.category(), Color.GRAY);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = new Color(base.getRed(), base.getGreen(), base.getBlue(), 180);
        g.setColor(fill);

        PlaceholderShape shape = shapeForCategory(layer.category(), width, height);
        g.fillRoundRect(shape.x(), shape.y(), shape.w(), shape.h(), shape.arc(), shape.arc());

        Color outline = base.darker().darker();
        g.setColor(new Color(outline.getRed(), outline.getGreen(), outline.getBlue(), 220));
        g.drawRoundRect(shape.x(), shape.y(), shape.w(), shape.h(), shape.arc(), shape.arc());

        g.dispose();
        return img;
    }

    private record PlaceholderShape(int x, int y, int w, int h, int arc) {}

    private PlaceholderShape shapeForCategory(SpriteLayerCategory category, int width, int height) {
        int cx = width / 2;
        int cy = height / 2;
        int headH = height / 5;

        return switch (category) {
            case BASE_BODY -> new PlaceholderShape(cx - width / 6, cy - height / 4, width / 3, height / 2, 6);
            case SHOES -> new PlaceholderShape(cx - width / 5, cy + height / 4, width * 2 / 5, height / 8, 4);
            case CLOTHING_BOTTOM -> new PlaceholderShape(cx - width / 5, cy, width * 2 / 5, height / 3, 4);
            case CLOTHING_TOP -> new PlaceholderShape(cx - width / 4, cy - height / 8, width / 2, height / 3, 6);
            case EYES -> new PlaceholderShape(cx - width / 8, cy - height / 4, width / 4, headH / 3, 8);
            case HAIR -> new PlaceholderShape(cx - width / 5, cy - height / 3, width * 2 / 5, headH, 10);
            case ACCESSORY -> new PlaceholderShape(cx - width / 6, cy - height / 5, width / 3, headH / 2, 4);
        };
    }

    private CharacterDefinition resolveCharacter(String name) {
        return switch (name.toLowerCase()) {
            case "husband" -> CharacterDefinition.husband();
            default        -> CharacterDefinition.tatyana();
        };
    }
}
