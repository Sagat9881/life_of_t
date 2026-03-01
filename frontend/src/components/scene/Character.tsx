/**
 * Character - Simplified PixiJS initialization
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

    console.log('[Character] Initializing PixiJS (synchronous)...');

    try {
      // Synchronous initialization (legacy but reliable)
      const app = new PIXI.Application({
        width: 200,
        height: 300,
        backgroundAlpha: 0,
        antialias: true,
        resolution: window.devicePixelRatio || 1,
      });

      appRef.current = app;
      console.log('[Character] PixiJS app created');

      if (!canvasRef.current) {
        console.error('[Character] Canvas ref lost after app creation');
        setError('Canvas ref lost');
        return;
      }

      // Append canvas
      canvasRef.current.appendChild(app.view as HTMLCanvasElement);
      console.log('[Character] Canvas appended to DOM');

      // Main character container
      const character = new PIXI.Container();
      character.x = 100;
      character.y = 270;
      app.stage.addChild(character);
      console.log('[Character] Container created');

      // TEST: Big red circle to verify rendering
      const testCircle = new PIXI.Graphics();
      testCircle.beginFill(0xFF0000); // RED
      testCircle.drawCircle(0, -100, 40);
      testCircle.endFill();
      character.addChild(testCircle);
      console.log('[Character] Test RED circle added');

      // Draw head
      const g = new PIXI.Graphics();

      // Colors
      const skinBase = 0xF5D5B8;
      const hairBase = 0x8B1A1A;
      const hairDark = 0x6B1515;

      // NECK
      g.beginFill(skinBase);
      g.drawRect(-8, -15, 16, 18);
      g.endFill();

      // FACE
      g.beginFill(skinBase);
      g.drawEllipse(0, -45, 22, 28);
      g.endFill();

      // HAIR BACK
      g.beginFill(hairDark);
      g.drawEllipse(0, -58, 28, 30);
      g.endFill();

      // HAIR LEFT
      g.beginFill(hairBase);
      g.drawRoundedRect(-28, -55, 14, 40, 8);
      g.endFill();

      // HAIR RIGHT
      g.beginFill(hairBase);
      g.drawRoundedRect(14, -55, 14, 40, 8);
      g.endFill();

      character.addChild(g);
      console.log('[Character] Head drawn');

      // Breathing animation
      if (state === 'idle') {
        let time = 0;
        app.ticker.add(() => {
          time += 0.02;
          character.y = 270 + Math.sin(time) * 3;
        });
        console.log('[Character] Animation started');
      }

      setLoaded(true);
      console.log('[Character] ✅ FULLY LOADED');

    } catch (err: any) {
      console.error('[Character] ❌ ERROR:', err);
      setError(err.message || 'Unknown error');
    }

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
          background: 'rgba(0, 255, 0, 0.5)',
          border: '3px solid lime',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'black',
          fontSize: '14px',
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