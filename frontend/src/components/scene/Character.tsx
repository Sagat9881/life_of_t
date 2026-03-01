/**
 * Character - Tatyana character representation without sprite images
 * Based on tatyana.xml and character-visual-specs.xml
 */

import React from 'react';
import styles from './Character.module.css';
import { Position } from '../../types/SceneModel';

interface CharacterProps {
  position: Position;
  state: 'idle' | 'walking' | 'working' | 'sleeping';
  emotion: 'neutral' | 'happy' | 'sad' | 'tired' | 'love' | 'focused';
}

export const Character: React.FC<CharacterProps> = ({
  position,
  state,
  emotion,
}) => {
  const style: React.CSSProperties = {
    left: `${position.x}px`,
    top: `${position.y}px`,
  };

  return (
    <div
      className={`${styles.character} ${styles[state]} ${styles[emotion]}`}
      style={style}
    >
      {/* Head */}
      <div className={styles.head}>
        <div className={styles.hair} />
        <div className={styles.face}>
          <div className={styles.eyes} />
          <div className={styles.mouth} />
        </div>
      </div>

      {/* Body */}
      <div className={styles.body}>
        <div className={styles.torso} />
        <div className={styles.arms} />
      </div>

      {/* Shadow */}
      <div className={styles.shadow} />
    </div>
  );
};