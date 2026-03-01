/**
 * Character - PixiJS v8 HEAD + TORSO (Layers 1-2)
 */

import React, { useEffect, useRef, useState } from 'react';
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
  const [error, setError] = useState<string | null>(null);
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    console.log('[Character] Component mounted');
    
    if (!canvasRef.current) {
      console.error('[Character] Canvas ref is null');
      setError('Canvas ref is null');
      return;
    }

    console.log('[Character] Initializing PixiJS v8...');

    let app: PIXI.Application;

    (async () => {
      try {
        app = new PIXI.Application();
        await app.init({
          width: 200,
          height: 300,
          backgroundAlpha: 0,
          antialias: true,
          resolution: window.devicePixelRatio || 1,
        });

        appRef.current = app;
        console.log('[Character] PixiJS app initialized');

        if (!canvasRef.current) {
          console.error('[Character] Canvas ref lost after init');
          setError('Canvas ref lost');
          return;
        }

        canvasRef.current.appendChild(app.canvas);
        console.log('[Character] Canvas appended to DOM');

        // Main character container
        const character = new PIXI.Container();
        character.x = 100;
        character.y = 180; // raised higher in canvas
        app.stage.addChild(character);
        console.log('[Character] Container created');

        const g = new PIXI.Graphics();

        // Colors
        const skinBase = 0xF5D5B8;
        const skinShadow = 0xF0CCAA;
        const hairBase = 0x8B1A1A;
        const hairDark = 0x6B1515;
        const shirtWhite = 0xFFFFFF;
        const shirtShadow = 0xEEEEEE;

        // === LAYER 2: TORSO + ARMS ===

        // TORSO (white t-shirt)
        g.roundRect(-18, 5, 36, 45, 3);
        g.fill({ color: shirtWhite });
        // Torso shadow
        g.rect(-18, 45, 36, 5);
        g.fill({ color: shirtShadow, alpha: 0.3 });

        // LEFT ARM
        g.roundRect(-30, 8, 14, 35, 6);
        g.fill({ color: shirtWhite });
        // Left hand
        g.ellipse(-23, 42, 6, 7);
        g.fill({ color: skinBase });

        // RIGHT ARM
        g.roundRect(16, 8, 14, 35, 6);
        g.fill({ color: shirtWhite });
        // Right hand
        g.ellipse(23, 42, 6, 7);
        g.fill({ color: skinBase });

        // === LAYER 1: HEAD ===

        // NECK
        g.rect(-8, -15, 16, 18);
        g.fill({ color: skinBase });
        g.rect(-7, -15, 14, 3);
        g.fill({ color: skinShadow, alpha: 0.3 });

        // FACE
        g.ellipse(0, -45, 22, 28);
        g.fill({ color: skinBase });
        g.ellipse(-5, -55, 12, 15);
        g.fill({ color: 0xFFFFFF, alpha: 0.15 });
        g.ellipse(3, -35, 15, 18);
        g.fill({ color: skinShadow, alpha: 0.2 });

        // HAIR BACK
        g.ellipse(0, -58, 28, 30);
        g.fill({ color: hairDark });
        g.ellipse(0, -50, 26, 8);
        g.fill({ color: 0x000000, alpha: 0.2 });

        // HAIR LEFT
        g.roundRect(-28, -55, 14, 40, 8);
        g.fill({ color: hairBase });
        g.roundRect(-28, -55, 14, 40, 8);
        g.stroke({ color: hairDark, width: 1 });

        // HAIR RIGHT
        g.roundRect(14, -55, 14, 40, 8);
        g.fill({ color: hairBase });
        g.roundRect(14, -55, 14, 40, 8);
        g.stroke({ color: hairDark, width: 1 });

        // BANGS
        g.moveTo(-20, -65);
        g.bezierCurveTo(-15, -72, 15, -72, 20, -65);
        g.lineTo(20, -58);
        g.bezierCurveTo(15, -60, -15, -60, -20, -58);
        g.closePath();
        g.fill({ color: hairBase });

        character.addChild(g);
        console.log('[Character] Head + Torso drawn (Layers 1-2)');

        // Breathing animation
        if (state === 'idle') {
          let time = 0;
          app.ticker.add(() => {
            time += 0.02;
            character.y = 180 + Math.sin(time) * 3;
          });
          console.log('[Character] Animation started');
        }

        setLoaded(true);
        console.log('[Character] ✅ FULLY LOADED');

      } catch (err: any) {
        console.error('[Character] ❌ ERROR:', err);
        setError(err.message || 'Unknown error');
      }
    })();

    return () => {
      console.log('[Character] Cleaning up');
      if (appRef.current) {
        appRef.current.destroy(true, { children: true });
      }
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
    >
      {!loaded && !error && (
        <div style={{
          width: '200px',
          height: '300px',
          background: 'rgba(0, 255, 0, 0.2)',
          border: '2px dashed lime',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'black',
          fontSize: '12px',
          fontWeight: 'bold',
        }}>
          🔄 LOADING...
        </div>
      )}

      {error && (
        <div style={{
          width: '200px',
          height: '300px',
          background: 'rgba(255, 0, 0, 0.9)',
          border: '3px solid darkred',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontSize: '12px',
          padding: '10px',
          textAlign: 'center',
          flexDirection: 'column',
          gap: '10px',
        }}>
          <div>❌ ERROR</div>
          <div>{error}</div>
        </div>
      )}

      <div ref={canvasRef} />
    </div>
  );
};