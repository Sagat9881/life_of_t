package ru.lifegame.assets.domain.service;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Strategy interface for rendering pixel-art sprite frames.
 * Each implementation draws a specific entity (character, pet, furniture, location).
 *
 * <p>Contract: returned frames must be exactly {@code frameWidth × frameHeight}
 * pixels, TYPE_INT_ARGB, with no anti-aliasing applied.</p>
 */
public interface PixelArtRenderer {

    /**
     * Unique renderer identifier matching the sprite-spec id
     * (e.g. "tanya_idle", "sam_walk", "bed_static").
     */
    String spriteId();

    /**
     * Generates all animation frames for this sprite.
     *
     * @param frameWidth  width of a single frame in pixels
     * @param frameHeight height of a single frame in pixels
     * @param frameCount  number of frames to generate
     * @return list of ARGB BufferedImages, one per frame
     */
    List<BufferedImage> renderFrames(int frameWidth, int frameHeight, int frameCount);
}
