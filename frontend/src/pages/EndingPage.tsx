import { useNavigate } from 'react-router-dom';
import { useGameStore } from '@/store/gameStore';
import { Button } from '@/components/shared/Button';
import styles from './EndingPage.module.css';

const ENDING_EMOJIS: Record<string, string> = {
  good: '🌟',
  bad: '💔',
  neutral: '🌸',
  great: '✨',
  burnout: '😔',
  love: '💕',
};

export function EndingPage() {
  const { gameState, startGame } = useGameStore();
  const navigate = useNavigate();
  const ending = gameState?.ending;

  const handleRestart = async () => {
    await startGame();
    navigate('/room');
  };

  if (!ending) {
    return (
      <div className={styles.noEnding}>
        <p>Игра ещё не завершена</p>
        <Button variant="primary" onClick={() => navigate('/room')}>
          Вернуться в игру
        </Button>
      </div>
    );
  }

  const emoji = ENDING_EMOJIS[ending.type] ?? '🌸';

  return (
    <div className={styles.page}>
      <div className={styles.content}>
        <div className={styles.emojiLarge}>{emoji}</div>
        <div className={styles.dayCount}>
          День {gameState?.time.day ?? 0}
        </div>
        <h1 className={styles.title}>{ending.title}</h1>
        <p className={styles.description}>{ending.description}</p>

        <div className={styles.actions}>
          <Button variant="primary" size="lg" fullWidth onClick={handleRestart}>
            Начать заново
          </Button>
          <Button variant="ghost" fullWidth onClick={() => navigate('/stats')}>
            Посмотреть итоги
          </Button>
        </div>
      </div>

      {/* Decorative particles */}
      <div className={styles.particles} aria-hidden="true">
        {Array.from({ length: 12 }).map((_, i) => (
          <span key={i} className={styles.particle} style={{ '--i': i } as React.CSSProperties}>
            ✨
          </span>
        ))}
      </div>
    </div>
  );
}
