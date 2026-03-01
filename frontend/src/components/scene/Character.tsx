/**
 * Character - Detailed CSS-based Tatyana sprite
 * Based on character-visual-specs.txt and FRONTEND_SYSTEM_PROMPT.md
 */

import React from 'react';
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
  return (
    <div
      className={`${styles.character} ${styles[state]} ${styles[emotion]}`}
      style={{
        left: `${position.x}px`,
        top: `${position.y}px`,
        zIndex: position.zIndex,
      }}
    >
      {/* Head */}
      <div className={styles.head}>
        {/* Hair */}
        <div className={styles.hair}>
          <div className={styles.hairLeft} />
          <div className={styles.hairRight} />
          <div className={styles.hairBack} />
        </div>

        {/* Face */}
        <div className={styles.face}>
          {/* Eyes */}
          <div className={styles.eyes}>
            <div className={styles.eye}>
              <div className={styles.pupil} />
            </div>
            <div className={styles.eye}>
              <div className={styles.pupil} />
            </div>
          </div>

          {/* Smile */}
          <div className={styles.smile} />

          {/* Blush */}
          <div className={styles.blush} />
        </div>
      </div>

      {/* Body */}
      <div className={styles.body}>
        {/* Necklace */}
        <div className={styles.necklace}>
          <div className={styles.chain} />
          <div className={styles.heart} />
        </div>

        {/* Sweater */}
        <div className={styles.sweater}>
          <div className={styles.leftArm} />
          <div className={styles.rightArm} />
        </div>

        {/* Jeans */}
        <div className={styles.jeans}>
          <div className={styles.leftLeg} />
          <div className={styles.rightLeg} />
        </div>

        {/* Slippers */}
        <div className={styles.slippers}>
          <div className={styles.leftSlipper} />
          <div className={styles.rightSlipper} />
        </div>
      </div>

      {/* Shadow */}
      <div className={styles.shadow} />
    </div>
  );
};