import { type HTMLAttributes, type ReactNode } from 'react';
import styles from './Card.module.css';

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'elevated' | 'bordered';
  title?: string;
  children: ReactNode;
}

export function Card({ variant = 'default', title, children, className = '', ...props }: CardProps) {
  return (
    <div className={[styles.card, styles[variant], className].filter(Boolean).join(' ')} {...props}>
      {title && <h3 className={styles.title}>{title}</h3>}
      {children}
    </div>
  );
}
