/**
 * LPCCharacter - Universal LPC Spritesheet Character Component
 * Supports 64x64 sprite animations from LPC Generator
 * Animations: walk (4 directions), idle, attack, spellcast
 */

import React, { useEffect, useRef, useState } from 'react';
import * as PIXI from 'pixi.js';
import styles from './Character.module.css';

interface LPCCharacterProps {
  position: { x: number; y: number; zIndex: number };
  spritesheet: string; // Path to spritesheet image
  state?: 'idle' | 'walk' | 'work' | 'sleep';
  emotion?: 'neutral' | 'happy' | 'sad' | 'tired';
  direction?: 'south' | 'west' | 'east' | 'north'; // LPC uses 4 directions
}

// LPC Spritesheet layout configuration
const LPC_CONFIG = {
  frameWidth: 64,
  frameHeight: 64,
  // Row indices for different animations (standard LPC layout)
  animations: {
    spellcast: { row: 0, frames: 7 },
    thrust: { row: 1, frames: 8 },
    walk: {
      north: { row: 8, frames: 9 },
      west: { row: 9, frames: 9 },
      south: { row: 10, frames: 9 },
      east: { row: 11, frames: 9 },
    },
    slash: { row: 12, frames: 6 },
    shoot: { row: 13, frames: 13 },
    hurt: { row: 20, frames: 6 },
  },
  fps: 12, // Animation speed
};

export const LPCCharacter: React.FC<LPCCharacterProps> = ({
  position,
  spritesheet,
  state = 'idle',
  emotion = 'neutral',
  direction = 'south',
}) => {
  const canvasRef = useRef<HTMLDivElement>(null);
  const appRef = useRef<PIXI.Application | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const animationRef = useRef<PIXI.AnimatedSprite | null>(null);

  useEffect(() => {
    console.log('[LPCCharacter] Component mounted', { state, direction });
    
    if (!canvasRef.current) {
      console.error('[LPCCharacter] Canvas ref is null');
      setError('Canvas ref is null');
      return;
    }

    let app: PIXI.Application;
    let destroyed = false;

    (async () => {
      try {
        // Initialize PixiJS v8
        app = new PIXI.Application();
        await app.init({
          width: 128,
          height: 128,
          backgroundAlpha: 0,
          antialias: false, // Pixel art should be crisp
          resolution: window.devicePixelRatio || 1,
        });

        if (destroyed) return;

        appRef.current = app;
        console.log('[LPCCharacter] PixiJS app initialized');

        if (!canvasRef.current) {
          console.error('[LPCCharacter] Canvas ref lost after init');
          setError('Canvas ref lost');
          return;
        }

        canvasRef.current.appendChild(app.canvas);

        // Load spritesheet
        console.log('[LPCCharacter] Loading spritesheet:', spritesheet);
        const texture = await PIXI.Assets.load(spritesheet);
        
        if (destroyed) return;

        console.log('[LPCCharacter] Spritesheet loaded successfully');

        // Create frames for animation based on state and direction
        const frames: PIXI.Texture[] = [];
        let animConfig;

        if (state === 'walk') {
          animConfig = LPC_CONFIG.animations.walk[direction];
        } else if (state === 'idle') {
          // Use first frame of walk animation for idle
          animConfig = { row: LPC_CONFIG.animations.walk[direction].row, frames: 1 };
        } else {
          // Default to south walk first frame
          animConfig = { row: LPC_CONFIG.animations.walk.south.row, frames: 1 };
        }

        console.log('[LPCCharacter] Animation config:', animConfig);

        // Extract frames from spritesheet
        for (let i = 0; i < animConfig.frames; i++) {
          const frameTexture = new PIXI.Texture({
            source: texture.source,
            frame: new PIXI.Rectangle(
              i * LPC_CONFIG.frameWidth,
              animConfig.row * LPC_CONFIG.frameHeight,
              LPC_CONFIG.frameWidth,
              LPC_CONFIG.frameHeight
            ),
          });
          frames.push(frameTexture);
        }

        console.log('[LPCCharacter] Created', frames.length, 'frames');

        // Create animated sprite
        const sprite = new PIXI.AnimatedSprite(frames);
        sprite.animationSpeed = LPC_CONFIG.fps / 60; // Convert FPS to PixiJS speed
        sprite.x = 64; // Center in canvas
        sprite.y = 64;
        sprite.anchor.set(0.5, 0.5);
        sprite.scale.set(1.5); // Scale up for better visibility
        
        if (state === 'walk' && animConfig.frames > 1) {
          sprite.play();
          console.log('[LPCCharacter] Walk animation playing');
        } else {
          sprite.gotoAndStop(0);
          console.log('[LPCCharacter] Idle state (single frame)');
        }

        animationRef.current = sprite;
        app.stage.addChild(sprite);

        setLoading(false);
        console.log('[LPCCharacter] ✅ Sprite added to stage');

      } catch (err: any) {
        console.error('[LPCCharacter] ❌ ERROR:', err);
        setError(err.message || 'Failed to load spritesheet');
        setLoading(false);
      }
    })();

    return () => {
      destroyed = true;
      console.log('[LPCCharacter] Cleaning up');
      if (animationRef.current) {
        animationRef.current.stop();
        animationRef.current = null;
      }
      if (appRef.current) {
        appRef.current.destroy(true, { children: true });
        appRef.current = null;
      }
    };
  }, [spritesheet, state, direction]);

  return (
    <div
      className={styles.characterContainer}
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
        zIndex: position.zIndex,
      }}
    >
      {loading && (
        <div style={{
          width: '128px',
          height: '128px',
          background: 'rgba(0, 0, 0, 0.7)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontSize: '12px',
        }}>
          Loading...
        </div>
      )}

      {error && (
        <div style={{
          width: '128px',
          height: '128px',
          background: 'rgba(255, 0, 0, 0.9)',
          border: '3px solid darkred',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontSize: '12px',
          padding: '10px',
          textAlign: 'center',
        }}>
          ❌ {error}
        </div>
      )}

      <div ref={canvasRef} />
    </div>
  );
};