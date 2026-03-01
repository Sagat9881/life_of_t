/**
 * NPC - Non-player character component (Sam the dog, Garfield)
 */

import React from 'react';
import { NPC as NPCType } from '../../types/SceneModel';
import styles from './NPC.module.css';

interface NPCProps {
  npc: NPCType;
}

export const NPC: React.FC<NPCProps> = ({ npc }) => {
  const { id, position, sprite } = npc;

  const style: React.CSSProperties = {
    left: `${position.x}px`,
    top: `${position.y}px`,
    width: `${sprite.size.width}px`,
    height: `${sprite.size.height}px`,
  };

  return (
    <div className={`${styles.npc} ${styles[id]}`} style={style}>
      <div className={styles.npcShape} />
      <div className={styles.shadow} />
    </div>
  );
};