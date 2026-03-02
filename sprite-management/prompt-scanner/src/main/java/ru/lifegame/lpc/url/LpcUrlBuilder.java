package ru.lifegame.lpc.url;

import lombok.extern.slf4j.Slf4j;
import ru.lifegame.lpc.model.LpcCharacterConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Builds LPC Generator URLs with hash parameters.
 * <p>
 * Example URLs:
 * - https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/#sex=male&body=Body_Color_light
 * - https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/#sex=female&body=Body_Color_light&head=Human_Female_light&hair=Shoulder_burgundy
 */
@Slf4j
public class LpcUrlBuilder {

    private static final String BASE_URL = 
        "https://liberatedpixelcup.github.io/Universal-LPC-Spritesheet-Character-Generator/";

    /**
     * Build full LPC Generator URL from character config
     *
     * @param config Character configuration
     * @return Full URL with hash parameters
     */
    public String build(LpcCharacterConfig config) {
        List<String> params = new ArrayList<>();

        // Required: sex
        if (config.getSex() != null) {
            params.add("sex=" + encode(config.getSex()));
        }

        // Required: body
        if (config.getBody() != null) {
            params.add("body=" + encode(config.getBody()));
        }

        // Required: head
        if (config.getHead() != null) {
            params.add("head=" + encode(config.getHead()));
        }

        // Optional: expression
        if (config.getExpression() != null) {
            params.add("expression=" + encode(config.getExpression()));
        }

        // Hair layers
        if (config.getHair() != null && !config.getHair().isEmpty()) {
            for (String hairLayer : config.getHair()) {
                params.add("hair=" + encode(hairLayer));
            }
        }

        // Clothing layers
        addClothingLayers(params, config);

        // Accessories
        addAccessories(params, config);

        // Build final URL
        String hashParams = String.join("&", params);
        String url = BASE_URL + "#" + hashParams;

        log.debug("Built LPC URL: {}", url);
        return url;
    }

    /**
     * Add clothing layers to parameters
     */
    private void addClothingLayers(List<String> params, LpcCharacterConfig config) {
        Map<String, List<String>> clothing = config.getClothing();
        if (clothing == null || clothing.isEmpty()) {
            return;
        }

        // Tops
        if (clothing.containsKey("tops")) {
            for (String top : clothing.get("tops")) {
                params.add("torso=" + encode(top));
            }
        }

        // Bottoms
        if (clothing.containsKey("bottoms")) {
            for (String bottom : clothing.get("bottoms")) {
                params.add("legs=" + encode(bottom));
            }
        }

        // Shoes
        if (clothing.containsKey("shoes")) {
            for (String shoe : clothing.get("shoes")) {
                params.add("feet=" + encode(shoe));
            }
        }

        // Hands/Gloves
        if (clothing.containsKey("hands")) {
            for (String hand : clothing.get("hands")) {
                params.add("hands=" + encode(hand));
            }
        }
    }

    /**
     * Add accessory layers to parameters
     */
    private void addAccessories(List<String> params, LpcCharacterConfig config) {
        Map<String, List<String>> accessories = config.getAccessories();
        if (accessories == null || accessories.isEmpty()) {
            return;
        }

        // Necklaces
        if (accessories.containsKey("necklaces")) {
            for (String necklace : accessories.get("necklaces")) {
                params.add("necklace=" + encode(necklace));
            }
        }

        // Bracelets/Bracers
        if (accessories.containsKey("bracers")) {
            for (String bracer : accessories.get("bracers")) {
                params.add("bracers=" + encode(bracer));
            }
        }

        // Earrings
        if (accessories.containsKey("earrings")) {
            for (String earring : accessories.get("earrings")) {
                params.add("ears=" + encode(earring));
            }
        }
    }

    /**
     * URL encode a parameter value
     */
    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to encode URL parameter: {}", value, e);
            return value; // Fallback to unencoded
        }
    }

    /**
     * Parse LPC URL hash parameters into config
     * Useful for reverse-engineering existing URLs
     *
     * @param url Full LPC Generator URL
     * @return Parsed character configuration
     */
    public LpcCharacterConfig parse(String url) {
        log.debug("Parsing LPC URL: {}", url);

        if (!url.contains("#")) {
            log.warn("URL does not contain hash parameters");
            return new LpcCharacterConfig();
        }

        String hashPart = url.substring(url.indexOf("#") + 1);
        String[] paramPairs = hashPart.split("&");

        LpcCharacterConfig config = new LpcCharacterConfig();

        for (String pair : paramPairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length != 2) {
                continue;
            }

            String key = kv[0];
            String value = decode(kv[1]);

            switch (key) {
                case "sex" -> config.setSex(value);
                case "body" -> config.setBody(value);
                case "head" -> config.setHead(value);
                case "expression" -> config.setExpression(value);
                case "hair" -> config.addHair(value);
                // TODO: Add more parameter mappings
            }
        }

        log.debug("Parsed config: {}", config);
        return config;
    }

    /**
     * URL decode a parameter value
     */
    private String decode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to decode URL parameter: {}", value, e);
            return value;
        }
    }
}
