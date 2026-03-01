/**
 * Character - PixiJS-based Tatyana sprite
 * Renders character on Canvas using PixiJS Graphics API
 * Based on reference image: burgundy hair, mint top, beige cardigan
 */

import React, { useEffect, useRef } from 'react';
import * as PIXI from 'pixi.js';
import styles from './Character.module.css';

interface CharacterProps {
  position: { x: number; y: number; zIndex: number };
  state?: 'idle' | 'walk' | 'work' | 'sleep';
  emotion?: 'neutral' | 'happy' | 'sad' | 'tired';
}

export const Character: React.FC<CharacterProps> = ({
  position,
  state = 'idle',
  emotion = 'neutral',
}) => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const appRef = useRef<PIXI.Application | null>(null);

  useEffect(() => {
    if (!canvasRef.current) return;

    // Initialize PixiJS Application
    const app = new PIXI.Application();
    appRef.current = app;

    (async () => {
      await app.init({
        width: 128,
        height: 192,
        backgroundAlpha: 0,
        antialias: true,
        resolution: window.devicePixelRatio || 1,
      });

      canvasRef.current?.appendChild(app.canvas);

      // Create character container
      const character = new PIXI.Container();
      character.x = 64;
      character.y = 160;
      app.stage.addChild(character);

      // Draw character using Graphics
      drawTatyana(character, emotion);

      // Idle animation
      if (state === 'idle') {
        let time = 0;
        app.ticker.add(() => {
          time += 0.03;
          character.y = 160 + Math.sin(time) * 2; // Breathing
        });
      }
    })();

    return () => {
      app.destroy(true, { children: true });
    };
  }, [state, emotion]);

  return (
    <div
      className={styles.characterContainer}
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
        zIndex: position.zIndex,
      }}
      ref={canvasRef}
    />
  );
};

/**
 * Draw Tatyana character using PixiJS Graphics
 * Matches reference image style
 */
function drawTatyana(container: PIXI.Container, emotion: string) {
  const graphics = new PIXI.Graphics();

  // Colors from reference
  const skinColor = 0xF5D5B8;
  const hairColor = 0x8B1A1A;
  const eyeColor = 0x6B8E23;
  const mintTopColor = 0xB5EAD7;
  const cardiganColor = 0xF5E6D3;
  const jeansColor = 0xE8E8E8;
  const slipperColor = 0xA8A8A8;

  // Shadow
  graphics.ellipse(0, 60, 20, 6);
  graphics.fill({ color: 0x000000, alpha: 0.2 });

  // === LEGS & FEET ===
  // Left leg
  graphics.rect(-8, 35, 8, 20);
  graphics.fill({ color: jeansColor });
  graphics.rect(-8, 35, 8, 20);
  graphics.stroke({ color: 0xD0D0D0, width: 1 });

  // Right leg
  graphics.rect(2, 35, 8, 20);
  graphics.fill({ color: jeansColor });
  graphics.rect(2, 35, 8, 20);
  graphics.stroke({ color: 0xD0D0D0, width: 1 });

  // Left slipper
  graphics.roundRect(-9, 54, 10, 6, 3);
  graphics.fill({ color: slipperColor });

  // Right slipper
  graphics.roundRect(1, 54, 10, 6, 3);
  graphics.fill({ color: slipperColor });

  // === BODY ===
  // Cardigan (beige)
  graphics.roundRect(-14, 10, 28, 28, 4);
  graphics.fill({ color: cardiganColor });
  graphics.roundRect(-14, 10, 28, 28, 4);
  graphics.stroke({ color: 0xE5D3BF, width: 1 });

  // Mint top (visible through open cardigan)
  graphics.roundRect(-8, 12, 16, 22, 3);
  graphics.fill({ color: mintTopColor });

  // Arms
  // Left arm
  graphics.roundRect(-18, 14, 6, 20, 3);
  graphics.fill({ color: cardiganColor });

  // Right arm
  graphics.roundRect(12, 14, 6, 20, 3);
  graphics.fill({ color: cardiganColor });

  // Hands
  graphics.circle(-15, 34, 3);
  graphics.fill({ color: skinColor });
  graphics.circle(15, 34, 3);
  graphics.fill({ color: skinColor });

  // Gold necklace
  graphics.moveTo(-6, 11);
  graphics.lineTo(6, 11);
  graphics.stroke({ color: 0xFFD700, width: 2 });
  
  // Heart pendant
  graphics.circle(0, 14, 2.5);
  graphics.fill({ color: 0xFFD700 });

  // === HEAD ===
  // Neck
  graphics.rect(-4, 6, 8, 6);
  graphics.fill({ color: skinColor });

  // Face (oval)
  graphics.ellipse(0, 0, 10, 12);
  graphics.fill({ color: skinColor });
  graphics.ellipse(0, 0, 10, 12);
  graphics.stroke({ color: 0xF0CCAA, width: 1 });

  // Hair back (burgundy)
  graphics.ellipse(0, -8, 12, 10);
  graphics.fill({ color: hairColor });

  // Hair left side
  graphics.roundRect(-12, -6, 6, 14, 4);
  graphics.fill({ color: hairColor });

  // Hair right side
  graphics.roundRect(6, -6, 6, 14, 4);
  graphics.fill({ color: hairColor });

  // Hair front bangs
  graphics.moveTo(-8, -10);
  graphics.bezierCurveTo(-6, -12, 6, -12, 8, -10);
  graphics.lineTo(8, -6);
  graphics.lineTo(-8, -6);
  graphics.closePath();
  graphics.fill({ color: hairColor });

  // === FACE DETAILS ===
  // Eyes (olive green)
  // Left eye white
  graphics.ellipse(-4, -2, 2.5, 3);
  graphics.fill({ color: 0xFFFFFF });
  // Left pupil
  graphics.circle(-4, -1.5, 1.5);
  graphics.fill({ color: eyeColor });
  // Left highlight
  graphics.circle(-4.5, -2, 0.5);
  graphics.fill({ color: 0xFFFFFF });

  // Right eye white
  graphics.ellipse(4, -2, 2.5, 3);
  graphics.fill({ color: 0xFFFFFF });
  // Right pupil
  graphics.circle(4, -1.5, 1.5);
  graphics.fill({ color: eyeColor });
  // Right highlight
  graphics.circle(3.5, -2, 0.5);
  graphics.fill({ color: 0xFFFFFF });

  // Eyebrows
  graphics.moveTo(-6, -5);
  graphics.bezierCurveTo(-5, -6, -3, -6, -2, -5);
  graphics.stroke({ color: 0x6B1515, width: 1.5 });

  graphics.moveTo(2, -5);
  graphics.bezierCurveTo(3, -6, 5, -6, 6, -5);
  graphics.stroke({ color: 0x6B1515, width: 1.5 });

  // Nose (subtle)
  graphics.circle(0, 2, 0.8);
  graphics.fill({ color: 0xF0CCAA });

  // Smile
  if (emotion === 'happy') {
    graphics.arc(0, 4, 4, 0.3, Math.PI - 0.3);
    graphics.stroke({ color: 0xD4A494, width: 2 });
  } else {
    graphics.arc(0, 4, 3, 0.5, Math.PI - 0.5);
    graphics.stroke({ color: 0xD4A494, width: 1.5 });
  }

  // Blush
  graphics.ellipse(-6, 2, 2, 1.5);
  graphics.fill({ color: 0xFFB6C1, alpha: 0.4 });
  graphics.ellipse(6, 2, 2, 1.5);
  graphics.fill({ color: 0xFFB6C1, alpha: 0.4 });

  container.addChild(graphics);
}