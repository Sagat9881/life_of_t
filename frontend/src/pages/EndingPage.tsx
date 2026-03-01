import React from 'react';
import styles from './HomePage.module.css';

export const EndingPage: React.FC = () => {
  return (
    <div className={styles.container}>
      <div className={styles.endingContainer}>
        <h1 className={styles.endingTitle}>🎭 Концовка</h1>
        <div className={styles.endingContent}>
          <p className={styles.endingText}>
            История твоей жизни завершилась...
          </p>
          <div className={styles.endingStats}>
            <h2>Итоги:</h2>
            <ul>
              <li>Прожито дней: 30</li>
              <li>Достижений разблокировано: 5/20</li>
              <li>Отношений развито: 3</li>
            </ul>
          </div>
          <div className={styles.endingButtons}>
            <button
              className={styles.restartButton}
              onClick={() => window.location.href = '/room'}
            >
              🔄 Начать заново
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
