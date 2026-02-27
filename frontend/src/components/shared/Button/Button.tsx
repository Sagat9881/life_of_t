import { type ButtonHTMLAttributes, type ReactNode } from 'react';
import { useHaptic } from '@/hooks/useHaptic';
import styles from './Button.module.css';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: 'primary' | 'secondary' | 'accent' | 'outline';
  size?: 'small' | 'medium' | 'large';
  fullWidth?: boolean;
  isLoading?: boolean;
}

export const Button = ({
  children,
  variant = 'primary',
  size = 'medium',
  fullWidth = false,
  isLoading = false,
  disabled,
  onClick,
  className = '',
  ...props
}: ButtonProps) => {
  const { impact } = useHaptic();

  const handleClick = (e: React.MouseEvent<HTMLButtonElement>) => {
    if (!disabled && !isLoading) {
      impact('light');
      onClick?.(e);
    }
  };

  const classNames = [
    styles.button,
    styles[variant],
    styles[size],
    fullWidth ? styles.fullWidth : '',
    isLoading ? styles.loading : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <button
      className={classNames}
      disabled={disabled || isLoading}
      onClick={handleClick}
      {...props}
    >
      {isLoading ? (
        <span className={styles.spinner}>
          <span className={styles.spinnerDot} />
          <span className={styles.spinnerDot} />
          <span className={styles.spinnerDot} />
        </span>
      ) : (
        children
      )}
    </button>
  );
};
