import { useRef, useState } from 'react';
import styles from './InteractiveObject.module.css';

interface InteractiveObjectProps {
  id: string;
  emoji: string;
  label: string;
  x: number;
  y: number;
  width: number;
  height: number;
  available?: boolean;
  isSelected?: boolean;
  onClick: (id: string) => void;
}

export function InteractiveObject({
  id,
  emoji,
  label,
  x,
  y,
  width,
  height,
  available = true,
  isSelected = false,
  onClick,
}: InteractiveObjectProps) {
  const buttonRef = useRef<HTMLButtonElement>(null);
  const [isClicking, setIsClicking] = useState(false);

  const handleClick = () => {
    if (!available) return;
    setIsClicking(true);
    onClick(id);
    setTimeout(() => setIsClicking(false), 300);
  };

  return (
    <button
      ref={buttonRef}
      className={`${styles.object} ${isSelected ? styles.active : ''} ${isClicking ? styles.clicking : ''}`}
      style={{
        left: `${x}%`,
        top: `${y}%`,
        width: `${width}px`,
        height: `${height}px`,
      }}
      data-available={available}
      onClick={handleClick}
      aria-label={label}
      title={label}
    >
      <span className={styles.sprite}>{emoji}</span>
      <span className={styles.label}>{label}</span>
    {isSelected && (
        <span className={styles.selectedRing} />
      )}
    </button>
  );
}

