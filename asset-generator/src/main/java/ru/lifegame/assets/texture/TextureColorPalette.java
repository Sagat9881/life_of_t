package ru.lifegame.assets.texture;

import java.awt.Color;

public record TextureColorPalette(Color fg, Color bg, Color outline) {

    public static TextureColorPalette wood() {
        return new TextureColorPalette(
                hex("#8B5E3C"),
                hex("#5C3A1E"),
                hex("#4A2E10")
        );
    }

    public static TextureColorPalette stone() {
        return new TextureColorPalette(
                hex("#9E9E9E"),
                hex("#616161"),
                hex("#424242")
        );
    }

    public static TextureColorPalette fabric() {
        return new TextureColorPalette(
                hex("#E91E8C"),
                hex("#C2185B"),
                hex("#880E4F")
        );
    }

    public static TextureColorPalette metal() {
        return new TextureColorPalette(
                hex("#CFD8DC"),
                hex("#78909C"),
                hex("#37474F")
        );
    }

    public static TextureColorPalette wallpaper() {
        return new TextureColorPalette(
                hex("#FFF8E1"),
                hex("#FFECB3"),
                hex("#FFD54F")
        );
    }

    public static Color hex(String hex) {
        if (hex == null || hex.isBlank()) throw new IllegalArgumentException("Colour hex must not be blank");
        String cleaned = hex.startsWith("#") ? hex.substring(1) : hex;
        if (cleaned.length() != 6) throw new IllegalArgumentException("Expected 6 hex digits, got: " + hex);
        int r = Integer.parseInt(cleaned.substring(0, 2), 16);
        int g = Integer.parseInt(cleaned.substring(2, 4), 16);
        int b = Integer.parseInt(cleaned.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    public static TextureColorPalette fromHex(String fgHex, String bgHex, String outlineHex) {
        TextureColorPalette defaults = wood();
        Color fg      = (fgHex      != null && !fgHex.isBlank())      ? hex(fgHex)      : defaults.fg();
        Color bg      = (bgHex      != null && !bgHex.isBlank())      ? hex(bgHex)      : defaults.bg();
        Color outline = (outlineHex != null && !outlineHex.isBlank()) ? hex(outlineHex) : defaults.outline();
        return new TextureColorPalette(fg, bg, outline);
    }
}
