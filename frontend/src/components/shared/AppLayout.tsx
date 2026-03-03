import { type ReactNode, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { HUD } from '@/components/hud/HUD';
import { BottomNav } from '@/components/shared/BottomNav';
import { useGameStore } from '@/store/gameStore';
import styles from './AppLayout.module.css';

interface AppLayoutProps {
  children: ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  const { gameState, toast, clearToast } = useGameStore();
  const navigate = useNavigate();

  // Navigate to ending screen when game ends
  useEffect(() => {
    if (gameState?.ending) {
      navigate('/ending');
    }
  }, [gameState?.ending, navigate]);

  return (
    <div className={styles.layout}>
      <HUD />
      <main className={styles.content}>
        {children}
      </main>
      <BottomNav />

      {/* Toast notification */}
      {toast && (
        <div
          className={`${styles.toast} ${styles[`toast-${toast.type}`]}`}
          onClick={clearToast}
          role="alert"
        >
          {toast.message}
        </div>
      )}
    </div>
  );
}

