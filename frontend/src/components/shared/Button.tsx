import { type ButtonHTMLAttributes, type ReactNode } from 'react';
import styles from './Button.module.css';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
  isLoading?: boolean;
  children: ReactNode;
}

export function Button({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  isLoading = false,
  disabled,
  children,
  className = '',
  ...rest
}: ButtonProps) {
  return (
    <button
      className={[
        styles.button,
        styles[variant],
        styles[size],
        fullWidth ? styles.full : '',
        isLoading ? styles.loading : '',
        className,
      ].filter(Boolean).join(' ')}
      disabled={disabled || isLoading}
      {...rest}
    >
      {isLoading && <span className={styles.spinner} />}
      <span className={isLoading ? styles.loadingText : ''}>{children}</span>
    </button>
  );
}
