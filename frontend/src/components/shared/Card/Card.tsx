import { type ReactNode, type CSSProperties } from 'react';
import styles from './Card.module.css';

interface CardProps {
  children: ReactNode;
  variant?: 'default' | 'elevated' | 'outlined';
  padding?: 'none' | 'small' | 'medium' | 'large';
  onClick?: () => void;
  className?: string;
  style?: CSSProperties;
}

export const Card = ({
  children,
  variant = 'default',
  padding = 'medium',
  onClick,
  className = '',
  style,
}: CardProps) => {
  const classNames = [
    styles.card,
    styles[variant],
    styles[`padding-${padding}`],
    onClick ? styles.clickable : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div 
      className={classNames} 
      onClick={onClick} 
      role={onClick ? 'button' : undefined}
      style={style}
    >
      {children}
    </div>
  );
};
