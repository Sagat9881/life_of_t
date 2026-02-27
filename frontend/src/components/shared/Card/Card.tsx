import { type ReactNode } from 'react';
import styles from './Card.module.css';

interface CardProps {
  children: ReactNode;
  variant?: 'default' | 'elevated' | 'outlined';
  padding?: 'none' | 'small' | 'medium' | 'large';
  onClick?: () => void;
  className?: string;
}

export const Card = ({
  children,
  variant = 'default',
  padding = 'medium',
  onClick,
  className = '',
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
    <div className={classNames} onClick={onClick} role={onClick ? 'button' : undefined}>
      {children}
    </div>
  );
};
