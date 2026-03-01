/**
 * Character - Layered PixiJS Tatyana sprite
 * Scale: 1/8 screen height (~250px tall character)
 * Rendering: 3 layers (background → mid → foreground)
 * Parts: HEAD → BODY → ARMS → HIPS → LEGS → FEET
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

    const app = new PIXI.Application();
    appRef.current = app;

    (async () => {
      await app.init({
        width: 200,
        height: 300,
        backgroundAlpha: 0,
        antialias: true,
        resolution: window.devicePixelRatio || 1,
      });

      canvasRef.current?.appendChild(app.canvas);

      // Main character container (centered)
      const character = new PIXI.Container();
      character.x = 100; // center X
      character.y = 270; // bottom anchor
      app.stage.addChild(character);

      // === LAYER 1: BACKGROUND (Base shapes) ===
      const layer1 = new PIXI.Container();
      character.addChild(layer1);

      // Draw HEAD - Layer 1 only (base shapes)
      drawHeadLayer1(layer1);

      // === LAYER 2: MID (Details) - TODO ===
      const layer2 = new PIXI.Container();
      character.addChild(layer2);

      // === LAYER 3: FOREGROUND (Fine details) - TODO ===
      const layer3 = new PIXI.Container();
      character.addChild(layer3);

      // Idle breathing animation
      if (state === 'idle') {
        let time = 0;
        app.ticker.add(() => {
          time += 0.02;
          character.y = 270 + Math.sin(time) * 3;
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
 * HEAD - Layer 1: Base shapes (skin, hair mass)
 * Scale: Head ~50px tall (1/5 of total character height)
 */
function drawHeadLayer1(container: PIXI.Container) {
  const g = new PIXI.Graphics();

  // Colors
  const skinBase = 0xF5D5B8;
  const skinShadow = 0xF0CCAA;
  const hairBase = 0x8B1A1A;
  const hairDark = 0x6B1515;

  // === NECK ===
  g.beginPath();
  g.rect(-8, -15, 16, 18);
  g.fill({ color: skinBase });
  // Neck shadow
  g.beginPath();
  g.rect(-7, -15, 14, 3);
  g.fill({ color: skinShadow, alpha: 0.3 });

  // === FACE BASE (oval) ===
  g.beginPath();
  g.ellipse(0, -45, 22, 28); // larger head
  g.fill({ color: skinBase });
  
  // Face highlight (top-left)
  g.beginPath();
  g.ellipse(-5, -55, 12, 15);
  g.fill({ color: 0xFFFFFF, alpha: 0.15 });

  // Face shadow (bottom-right)
  g.beginPath();
  g.ellipse(3, -35, 15, 18);
  g.fill({ color: skinShadow, alpha: 0.2 });

  // === HAIR MASS (back layer) ===
  // Back hair (large mass)
  g.beginPath();
  g.ellipse(0, -58, 28, 30); // behind head
  g.fill({ color: hairDark });

  // Hair shadow under
  g.beginPath();
  g.ellipse(0, -50, 26, 8);
  g.fill({ color: 0x000000, alpha: 0.2 });

  // Left hair volume
  g.beginPath();
  g.roundRect(-28, -55, 14, 40, 8);
  g.fill({ color: hairBase });
  g.beginPath();
  g.roundRect(-28, -55, 14, 40, 8);
  g.stroke({ color: hairDark, width: 1 });

  // Right hair volume
  g.beginPath();
  g.roundRect(14, -55, 14, 40, 8);
  g.fill({ color: hairBase });
  g.beginPath();
  g.roundRect(14, -55, 14, 40, 8);
  g.stroke({ color: hairDark, width: 1 });

  // Front hair (bangs placeholder)
  g.beginPath();
  g.moveTo(-20, -65);
  g.bezierCurveTo(-15, -72, 15, -72, 20, -65);
  g.lineTo(20, -58);
  g.bezierCurveTo(15, -60, -15, -60, -20, -58);
  g.closePath();
  g.fill({ color: hairBase });

  container.addChild(g);

  // TODO Layer 2: Hair strands, face contour
  // TODO Layer 3: Eyes, eyebrows, nose, mouth, blush
}