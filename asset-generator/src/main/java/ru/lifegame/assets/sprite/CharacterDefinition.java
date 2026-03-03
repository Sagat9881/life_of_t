package ru.lifegame.assets.sprite;

import java.util.Collections;
import java.util.List;

public record CharacterDefinition(String name, List<SpriteLayer> layers) {

    public CharacterDefinition {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("character name must not be blank");
        if (layers == null || layers.isEmpty()) throw new IllegalArgumentException("layers must not be empty");
        layers = Collections.unmodifiableList(layers);
    }

    public static CharacterDefinition tatyana() {
        return new CharacterDefinition("tatyana", List.of(
                SpriteLayer.of("female_base",    SpriteLayerCategory.BASE_BODY,       lpcPath("body/female/base.png")),
                SpriteLayer.of("sneakers_white", SpriteLayerCategory.SHOES,           lpcPath("shoes/female/sneakers_white.png")),
                SpriteLayer.of("jeans_blue",     SpriteLayerCategory.CLOTHING_BOTTOM, lpcPath("pants/female/jeans_blue.png")),
                SpriteLayer.of("top_casual_blue",SpriteLayerCategory.CLOTHING_TOP,    lpcPath("torso/female/blouse_blue.png")),
                SpriteLayer.of("eyes_blue",      SpriteLayerCategory.EYES,            lpcPath("eyes/female/blue.png")),
                SpriteLayer.of("hair_brown_long",SpriteLayerCategory.HAIR,            lpcPath("hair/female/long_brown.png"))
        ));
    }

    public static CharacterDefinition husband() {
        return new CharacterDefinition("husband", List.of(
                SpriteLayer.of("male_base",       SpriteLayerCategory.BASE_BODY,  lpcPath("body/male/base.png")),
                SpriteLayer.of("shoes_black",     SpriteLayerCategory.SHOES,      lpcPath("shoes/male/shoes_black.png")),
                SpriteLayer.of("trousers_dark",   SpriteLayerCategory.CLOTHING_BOTTOM, lpcPath("pants/male/trousers_dark.png")),
                SpriteLayer.of("shirt_grey",      SpriteLayerCategory.CLOTHING_TOP,    lpcPath("torso/male/shirt_grey.png")),
                SpriteLayer.of("eyes_brown",      SpriteLayerCategory.EYES,       lpcPath("eyes/male/brown.png")),
                SpriteLayer.of("hair_dark_short", SpriteLayerCategory.HAIR,       lpcPath("hair/male/short_dark.png")),
                SpriteLayer.of("glasses",         SpriteLayerCategory.ACCESSORY,  lpcPath("accessories/glasses.png"))
        ));
    }

    private static String lpcPath(String relative) {
        return "lpc-sprites/" + relative;
    }
}
