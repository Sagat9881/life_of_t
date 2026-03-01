import React, { useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import styles from './HomePage.module.css';

export const PetsPage: React.FC = () => {
  const { player, pets, isLoading, error, fetchGameState } = useGameStore();

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
        <h1 className={styles.title}>🐾 Питомцы</h1>
        <p className={styles.subtitle}>Твои любимые друзья</p>
      </div>

      <div className={styles.petsGrid}>
        {pets && pets.length > 0 ? (
          pets.map((pet) => (
            <div key={pet.id} className={styles.petCard}>
              <div className={styles.petHeader}>
                <span className={styles.petIcon}>{pet.species === 'Dog' ? '🐕' : '🐱'}</span>
                <h3 className={styles.petName}>{pet.name}</h3>
              </div>
              <div className={styles.petStats}>
                <div className={styles.petStat}>
                  <span className={styles.petStatLabel}>Настроение</span>
                  <div className={styles.progressBar}>
                    <div
                      className={styles.progressFill}
                      style={{
                        width: `${pet.mood}%`,
                        backgroundColor: pet.mood > 50 ? '#4CAF50' : '#FFA726',
                      }}
                    />
                  </div>
                  <span className={styles.petStatValue}>{pet.mood}/100</span>
                </div>
                <div className={styles.petStat}>
                  <span className={styles.petStatLabel}>Голод</span>
                  <div className={styles.progressBar}>
                    <div
                      className={styles.progressFill}
                      style={{
                        width: `${pet.hunger}%`,
                        backgroundColor: pet.hunger < 50 ? '#4CAF50' : '#E74C3C',
                      }}
                    />
                  </div>
                  <span className={styles.petStatValue}>{pet.hunger}/100</span>
                </div>
              </div>
              <div className={styles.petActions}>
                <button className={styles.petActionButton}>🥣 Покормить</button>
                <button className={styles.petActionButton}>🎾 Поиграть</button>
                <button className={styles.petActionButton}>🚶 Прогулка</button>
              </div>
            </div>
          ))
        ) : (
          <div className={styles.emptyState}>
            <p>У тебя пока нет питомцев</p>
          </div>
        )}
      </div>
    </div>
  );
};
