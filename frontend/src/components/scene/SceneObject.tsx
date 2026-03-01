/**
 * SceneObject - Renders an interactive object without image assets
 * Uses CSS shapes and colors based on object type
 */

import React from 'react';
import { InteractiveObject } from '../../types/SceneModel';
import styles from './SceneObject.module.css';

interface SceneObjectProps {
  object: InteractiveObject;
  state: string;
  onTap: () => void;
}

export const SceneObject: React.FC<SceneObjectProps> = ({
  object,
  state,
  onTap,
}) => {
  const { id, position, sprite, category } = object;

  const style: React.CSSProperties = {
    left: `${position.x}px`,
    top: `${position.y}px`,
    width: `${sprite.size.width}px`,
    height: `${sprite.size.height}px`,
  };

  return (
    <div
      className={`${styles.sceneObject} ${styles[category]} ${styles[id]}`}
      style={style}
      onClick={onTap}
      title={object.actions[0]?.label || id}
    >
      <div className={styles.objectShape} />
    </div>
  );
};