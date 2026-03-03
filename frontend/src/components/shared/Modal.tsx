import { useEffect, type ReactNode } from 'react';
import { X } from 'lucide-react';
import styles from './Modal.module.css';

interface ModalProps {
  isOpen: boolean;
  title: string;
  closeable?: boolean;
  onClose?: () => void;
  children: ReactNode;
}

export function Modal({ isOpen, title, closeable = true, onClose, children }: ModalProps) {
  // Prevent body scroll when modal is open
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
    return () => { document.body.style.overflow = ''; };
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={closeable ? onClose : undefined}>
      <div
        className={styles.sheet}
        onClick={e => e.stopPropagation()}
      >
        <div className={styles.header}>
          <h2 className={styles.title}>{title}</h2>
          {closeable && onClose && (
            <button className={styles.closeBtn} onClick={onClose}>
              <X size={20} />
            </button>
          )}
        </div>
        <div className={styles.content}>
          {children}
        </div>
      </div>
    </div>
  );
}
