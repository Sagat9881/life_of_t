/**
 * Character - Debug version with logging
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
      return;
    }

    console.log('[Character] Initializing PixiJS...');

    const app = new PIXI.Application();
    appRef.current = app;

    (async () => {
      try {
        await app.init({
          width: 200,
          height: 300,
          backgroundAlpha: 0,
          antialias: true,
          resolution: window.devicePixelRatio || 1,
        });

        console.log('[Character] PixiJS initialized');

        if (!canvasRef.current) {
          console.error('[Character] Canvas ref lost after init');
          return;
        }

        canvasRef.current.appendChild(app.canvas);
        console.log('[Character] Canvas appended to DOM');

        // Main character container
        const character = new PIXI.Container();
        character.x = 100;
        character.y = 270;
        app.stage.addChild(character);

        // Simple test shape - RED CIRCLE
        const testCircle = new PIXI.Graphics();
        testCircle.circle(0, -50, 30);
        testCircle.fill({ color: 0xFF0000 }); // RED
        character.addChild(testCircle);

        console.log('[Character] Test circle drawn');

        // Draw head
        const g = new PIXI.Graphics();

        // Colors
        const skinBase = 0xF5D5B8;
        const hairBase = 0x8B1A1A;
        const hairDark = 0x6B1515;

        // NECK
        g.rect(-8, -15, 16, 18);
        g.fill({ color: skinBase });

        // FACE
        g.ellipse(0, -45, 22, 28);
        g.fill({ color: skinBase });

        // HAIR BACK
        g.ellipse(0, -58, 28, 30);
        g.fill({ color: hairDark });

        // HAIR LEFT
        g.roundRect(-28, -55, 14, 40, 8);
        g.fill({ color: hairBase });

        // HAIR RIGHT
        g.roundRect(14, -55, 14, 40, 8);
        g.fill({ color: hairBase });

        // BANGS
        g.moveTo(-20, -65);
        g.bezierCurveTo(-15, -72, 15, -72, 20, -65);
        g.lineTo(20, -58);
        g.bezierCurveTo(15, -60, -15, -60, -20, -58);
        g.closePath();
        g.fill({ color: hairBase });

        character.addChild(g);

        console.log('[Character] Head drawn');

        // Breathing animation
        if (state === 'idle') {
          let time = 0;
          app.ticker.add(() => {
            time += 0.02;
            character.y = 270 + Math.sin(time) * 3;
          });
        }

        setLoaded(true);
        console.log('[Character] Fully loaded and animated');

      } catch (err: any) {
        console.error('[Character] PixiJS initialization failed:', err);
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
      {/* Fallback visible div */}
      {!loaded && !error && (
        <div style={{
          width: '200px',
          height: '300px',
          background: 'rgba(255, 0, 0, 0.3)',
          border: '3px solid red',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontSize: '14px',
          fontWeight: 'bold',
        }}>
          LOADING CHARACTER...
        </div>
      )}

      {error && (
        <div style={{
          width: '200px',
          height: '300px',
          background: 'rgba(255, 0, 0, 0.8)',
          border: '3px solid darkred',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontSize: '12px',
          padding: '10px',
          textAlign: 'center',
        }}>
          ERROR: {error}
        </div>
      )}

      <div ref={canvasRef} />
    </div>
  );
};