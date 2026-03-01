import React from 'react';
import styles from './Button.module.css';
import { useHaptic } from '../../../hooks/useHaptic';

export interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'danger' | 'accent' | 'outline';
  size?: 'small' | 'medium' | 'large';
  disabled?: boolean;
  isLoading?: boolean;
  fullWidth?: boolean;
  onClick?: () => void;
  className?: string;
  style?: React.CSSProperties;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'medium',
  disabled = false,
  isLoading = false,
  fullWidth = false,
  onClick,
  className = '',
  style,
}) => {
  const haptic = useHaptic();

  const handleClick = () => {
    if (!disabled && !isLoading && onClick) {
      haptic.light();
      onClick();
    }
  };

  return (
    <button
      className={`${styles.button} ${styles[variant]} ${styles[size]} ${fullWidth ? styles.fullWidth : ''} ${className}`}
      onClick={handleClick}
      disabled={disabled || isLoading}
      type="button"
      style={style}
    >
      {isLoading ? 'Загрузка...' : children}
    </button>
  );
};