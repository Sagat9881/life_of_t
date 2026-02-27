import { AlertCircle } from 'lucide-react';
import { Button } from '../Button';
import styles from './ErrorMessage.module.css';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
  size?: 'small' | 'medium' | 'large';
}

export const ErrorMessage = ({
  message,
  onRetry,
  size = 'medium',
}: ErrorMessageProps) => {
  return (
    <div className={`${styles.container} ${styles[size]}`}>
      <AlertCircle className={styles.icon} size={size === 'small' ? 24 : size === 'large' ? 48 : 32} />
      <p className={styles.message}>{message}</p>
      {onRetry && (
        <Button onClick={onRetry} variant="primary" size={size === 'large' ? 'medium' : 'small'}>
          Повторить
        </Button>
      )}
    </div>
  );
};
