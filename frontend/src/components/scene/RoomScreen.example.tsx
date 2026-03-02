/**
 * RoomScreen with LPC Character Integration - EXAMPLE
 * 
 * This is an example file showing how to integrate LPCCharacter.
 * To use: rename to RoomScreen.tsx or copy relevant parts.
 */

import React, { useState } from 'react';
import { LPCCharacter } from './LPCCharacter';
import { SceneObject } from './SceneObject';
import { NPC } from './NPC';
import styles from './SceneLayout.module.css';

export const RoomScreen: React.FC = () => {
  // Character state
  const [charPosition, setCharPosition] = useState({ x: 250, y: 200 });
  const [charState, setCharState] = useState<'idle' | 'walk'>('idle');
  const [charDirection, setCharDirection] = useState<'south' | 'north' | 'east' | 'west'>('south');
  const [emotion, setEmotion] = useState<'neutral' | 'happy' | 'sad' | 'tired'>('neutral');

  // Handle click-to-move
  const handleSceneClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;

    console.log('[RoomScreen] Click at', clickX, clickY);

    // Calculate direction
    const dx = clickX - charPosition.x;
    const dy = clickY - charPosition.y;
    
    let newDirection: 'south' | 'north' | 'east' | 'west' = 'south';
    
    if (Math.abs(dx) > Math.abs(dy)) {
      newDirection = dx > 0 ? 'east' : 'west';
    } else {
      newDirection = dy > 0 ? 'south' : 'north';
    }

    setCharDirection(newDirection);
    setCharState('walk');

    // Animate movement (simple lerp)
    const steps = 30;
    let currentStep = 0;
    
    const interval = setInterval(() => {
      currentStep++;
      const progress = currentStep / steps;
      
      setCharPosition({
        x: charPosition.x + dx * progress,
        y: charPosition.y + dy * progress,
      });

      if (currentStep >= steps) {
        clearInterval(interval);
        setCharState('idle');
        console.log('[RoomScreen] Movement complete');
      }
    }, 16); // ~60fps
  };

  return (
    <div className={styles.sceneContainer}>
      {/* Background */}
      <div className={styles.roomBackground}>
        <div className={styles.roomWalls} />
        <div className={styles.roomFloor} />
      </div>

      {/* Interactive scene layer */}
      <div 
        className={styles.interactiveLayer}
        onClick={handleSceneClick}
        style={{ cursor: 'pointer' }}
      >
        {/* Room Objects */}
        <SceneObject
          id="bed"
          position={{ x: 100, y: 100, zIndex: 2 }}
          type="bed"
        />
        
        <SceneObject
          id="computer"
          position={{ x: 400, y: 150, zIndex: 2 }}
          type="desk"
        />

        {/* Main Character - LPC Sprite */}
        <LPCCharacter
          position={{ ...charPosition, zIndex: 4 }}
          spritesheet="/assets/characters/tatyana/tatyana-lpc-base.png"
          state={charState}
          direction={charDirection}
          emotion={emotion}
        />

        {/* NPC - Husband (if present) */}
        {/* Uncomment when Sam's LPC sprite is ready:
        <LPCCharacter
          position={{ x: 450, y: 250, zIndex: 4 }}
          spritesheet="/assets/characters/sam/sam-lpc-base.png"
          state="idle"
          direction="west"
        />
        */}

        {/* Cat placeholder */}
        <NPC
          id="garfield"
          type="cat"
          position={{ x: 150, y: 300, zIndex: 3 }}
        />
      </div>

      {/* Debug Panel */}
      <div style={{
        position: 'absolute',
        top: '10px',
        right: '10px',
        background: 'rgba(0,0,0,0.8)',
        color: 'white',
        padding: '10px',
        borderRadius: '5px',
        fontSize: '12px',
        fontFamily: 'monospace',
        zIndex: 1000,
      }}>
        <div><strong>Debug Info</strong></div>
        <div>Position: {Math.round(charPosition.x)}, {Math.round(charPosition.y)}</div>
        <div>State: {charState}</div>
        <div>Direction: {charDirection}</div>
        <div>Emotion: {emotion}</div>
        <div style={{ marginTop: '10px' }}>
          <button onClick={() => setEmotion('happy')} style={{ marginRight: '5px' }}>😊</button>
          <button onClick={() => setEmotion('sad')} style={{ marginRight: '5px' }}>😢</button>
          <button onClick={() => setEmotion('tired')}>😴</button>
          <button onClick={() => setEmotion('neutral')} style={{ marginLeft: '5px' }}>😐</button>
        </div>
      </div>
    </div>
  );
};

export default RoomScreen;