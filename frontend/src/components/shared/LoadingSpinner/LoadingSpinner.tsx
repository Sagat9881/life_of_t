import styles from './LoadingSpinner.module.css';

interface LoadingSpinnerProps {
  size?: 'small' | 'medium' | 'large';
  color?: string;
  text?: string;
}

export const LoadingSpinner = ({
  size = 'medium',
  color,
  text,
}: LoadingSpinnerProps) => {
  const spinnerStyle = color ? { borderTopColor: color } : undefined;

  return (
    <div className={styles.container}>
      <div className={`${styles.spinner} ${styles[size]}`} style={spinnerStyle} />
      {text && <p className={styles.text}>{text}</p>}
    </div>
  );
};
