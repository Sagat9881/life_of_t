import React, { useEffect } from 'react';
import { useGameStore } from '../store/gameStore';
import styles from './HomePage.module.css';

export const StatsPage: React.FC = () => {
  const { player, time, isLoading, error, fetchGameState } = useGameStore();

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

  const stats = player.stats;
  const gameTime = time || { day: 1, hour: 12 };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>📊 Статистика</h1>
        <p className={styles.subtitle}>День {gameTime.day}, {gameTime.hour}:00</p>
      </div>

      <div className={styles.statsGrid}>
        {/* Main Stats */}
        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <span className={styles.statIcon}>⚡</span>
            <span className={styles.statLabel}>Энергия</span>
          </div>
          <div className={styles.statValue}>{stats.energy}/100</div>
          <div className={styles.progressBar}>
            <div
              className={styles.progressFill}
              style={{
                width: `${stats.energy}%`,
                backgroundColor: stats.energy > 50 ? '#4CAF50' : '#FFA726',
              }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <span className={styles.statIcon}>❤️</span>
            <span className={styles.statLabel}>Здоровье</span>
          </div>
          <div className={styles.statValue}>{stats.health}/100</div>
          <div className={styles.progressBar}>
            <div
              className={styles.progressFill}
              style={{
                width: `${stats.health}%`,
                backgroundColor: stats.health > 50 ? '#4CAF50' : '#E74C3C',
              }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <span className={styles.statIcon}>😊</span>
            <span className={styles.statLabel}>Настроение</span>
          </div>
          <div className={styles.statValue}>{stats.mood}/100</div>
          <div className={styles.progressBar}>
            <div
              className={styles.progressFill}
              style={{
                width: `${stats.mood}%`,
                backgroundColor: stats.mood > 50 ? '#FF6B9D' : '#FFA726',
              }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <span className={styles.statIcon}>😰</span>
            <span className={styles.statLabel}>Стресс</span>
          </div>
          <div className={styles.statValue}>{stats.stress}/100</div>
          <div className={styles.progressBar}>
            <div
              className={styles.progressFill}
              style={{
                width: `${stats.stress}%`,
                backgroundColor: stats.stress < 50 ? '#4CAF50' : '#E74C3C',
              }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <span className={styles.statIcon}>💪</span>
            <span className={styles.statLabel}>Самооценка</span>
          </div>
          <div className={styles.statValue}>{stats.selfEsteem}/100</div>
          <div className={styles.progressBar}>
            <div
              className={styles.progressFill}
              style={{
                width: `${stats.selfEsteem}%`,
                backgroundColor: stats.selfEsteem > 50 ? '#FF6B9D' : '#FFA726',
              }}
            />
          </div>
        </div>

        <div className={styles.statCard}>
          <div className={styles.statHeader}>
            <span className={styles.statIcon}>💰</span>
            <span className={styles.statLabel}>Деньги</span>
          </div>
          <div className={styles.statValue}>{stats.money} ₽</div>
        </div>
      </div>

      {/* Job Info */}
      {player.job && (
        <div className={styles.section}>
          <h2 className={styles.sectionTitle}>💼 Работа</h2>
          <div className={styles.jobInfo}>
            <div className={styles.jobTitle}>{player.job.title}</div>
            <div className={styles.jobCompany}>{player.job.company}</div>
            <div className={styles.jobSalary}>Зарплата: {player.job.salary} ₽/день</div>
          </div>
        </div>
      )}
    </div>
  );
};
