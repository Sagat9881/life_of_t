import React, { useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import styles from './HomePage.module.css';

export const QuestsPage: React.FC = () => {
  const { player, quests, isLoading, error, fetchGameState } = useGameStore();

  useEffect(() => {
    fetchGameState();
  }, [fetchGameState]);

  if (isLoading && !player) {
    return <div className={styles.loading}>Загрузка...</div>;
  }

  if (error) {
    return <div className={styles.error}>Ошибка: {error}</div>;
  }

  if (!player) {
    return <div className={styles.loading}>Нет данных об игроке</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>🎯 Квесты</h1>
        <p className={styles.subtitle}>Твои цели и достижения</p>
      </div>

      <div className={styles.questsGrid}>
        {quests && quests.length > 0 ? (
          quests.map((quest, index) => (
            <div key={quest.id || index} className={styles.questCard}>
              <div className={styles.questHeader}>
                <h3 className={styles.questTitle}>{quest.title || 'Квест'}</h3>
                <span className={styles.questStatus}>
                  {quest.completed ? '✅ Завершён' : '⏳ В процессе'}
                </span>
              </div>
              <p className={styles.questDescription}>{quest.description || 'Описание квеста'}</p>
              {quest.progress !== undefined && (
                <div className={styles.questProgress}>
                  <div className={styles.progressBar}>
                    <div
                      className={styles.progressFill}
                      style={{
                        width: `${quest.progress}%`,
                        backgroundColor: '#FF6B9D',
                      }}
                    />
                  </div>
                  <span className={styles.progressText}>{quest.progress}%</span>
                </div>
              )}
            </div>
          ))
        ) : (
          <div className={styles.emptyState}>
            <p>У тебя пока нет активных квестов</p>
          </div>
        )}
      </div>
    </div>
  );
};
