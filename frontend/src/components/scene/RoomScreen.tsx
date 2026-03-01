/**
 * RoomScreen - Main room location component
 * Based on RoomPage.xml and SCREENS_SPECIFICATION.xml
 */

import React from 'react';
import styles from './SceneLayout.module.css';
import { roomSceneModel } from '../../data/roomSceneModel';
import { SceneObject } from './SceneObject';
import { Character } from './Character';
import { NPC } from './NPC';

interface RoomScreenProps {
  gameState: any; // TODO: proper GameState type
  onObjectTap?: (objectId: string) => void;
}

export const RoomScreen: React.FC<RoomScreenProps> = ({
  gameState,
  onObjectTap,
}) => {
  const timeOfDay = gameState?.time?.timeOfDay || 'day';

  return (
    <div className={`${styles.sceneLayout} ${styles[timeOfDay]}`}>
      <div className={styles.sceneContainer}>
        <div className={styles.isometricView}>
          {/* Background layer */}
          <div className={styles.background} />

          {/* Back wall */}
          <div className={styles.backWall} />

          {/* Floor */}
          <div className={styles.floor} />

          {/* Interactive objects */}
          <div className={styles.objectsLayer}>
            {roomSceneModel.objects.map((obj) => (
              <SceneObject
                key={obj.id}
                object={obj}
                state={gameState?.objectStates?.[obj.id] || obj.states[0]?.name}
                onTap={() => onObjectTap?.(obj.id)}
              />
            ))}
          </div>

          {/* Character (Tatyana) */}
          <div className={styles.characterLayer}>
            <Character
              position={gameState?.character?.position || { x: 500, y: 400 }}
              state={gameState?.character?.state || 'idle'}
              emotion={gameState?.character?.emotion || 'neutral'}
            />
          </div>

          {/* NPCs (Sam, Garfield) */}
          <div className={styles.npcsLayer}>
            {roomSceneModel.npcs.map((npc) => (
              <NPC key={npc.id} npc={npc} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};