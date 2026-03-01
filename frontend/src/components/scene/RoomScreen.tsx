/**
 * RoomScreen - Main room scene without images, pure CSS
 */

import React from 'react';
import { SceneObject } from './SceneObject';
import { Character } from './Character';
import { NPC } from './NPC';
import { roomSceneModel } from '../../data/roomSceneModel';
import styles from './SceneLayout.module.css';

interface RoomScreenProps {
  gameState: any;
  onObjectTap: (objectId: string) => void;
}

export const RoomScreen: React.FC<RoomScreenProps> = ({
  gameState,
  onObjectTap,
}) => {
  const timeOfDay = gameState?.time?.timeOfDay || 'day';

  return (
    <div className={`${styles.sceneContainer} ${styles[timeOfDay]}`}>
      <div className={styles.sceneViewport}>
        {/* Isometric room structure */}
        <div className={styles.floor} />
        <div className={styles.backWall} />
        <div className={styles.leftWall} />
        <div className={styles.rightWall} />

        {/* Objects layer */}
        <div className={styles.objectsLayer}>
          {roomSceneModel.objects.map((obj) => (
            <SceneObject
              key={obj.id}
              object={obj}
              state={gameState?.objectStates?.[obj.id] || 'default'}
              onTap={() => onObjectTap(obj.id)}
            />
          ))}
        </div>

        {/* NPCs layer */}
        <div className={styles.npcsLayer}>
          {roomSceneModel.npcs.map((npc) => (
            <NPC key={npc.id} npc={npc} />
          ))}
        </div>

        {/* Character layer */}
        <div className={styles.charactersLayer}>
          <Character
            position={gameState?.character?.position || roomSceneModel.character.position}
            state={gameState?.character?.state || 'idle'}
            emotion={gameState?.character?.emotion || 'neutral'}
          />
        </div>
      </div>
    </div>
  );
};