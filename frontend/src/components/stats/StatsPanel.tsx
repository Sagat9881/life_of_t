import { StatKeyBar } from '@/components/shared/StatBar';
import type { Player } from '@/types/game';
import styles from './StatsPanel.module.css';

interface StatsPanelProps {
  player: Player;
}

export function StatsPanel({ player }: StatsPanelProps) {
  const { name, stats, job, location } = player;

  const jobSatisfactionColor = job.satisfaction > 60
    ? 'var(--color-success)'
    : job.satisfaction > 30
    ? 'var(--color-warning)'
    : 'var(--color-danger)';

  const burnoutColor = job.burnoutRisk > 70
    ? 'var(--color-danger)'
    : job.burnoutRisk > 40
    ? 'var(--color-warning)'
    : 'var(--color-success)';

  return (
    <div className={styles.panel}>
      {/* Profile header */}
      <div className={styles.profileHeader}>
        <div className={styles.avatar}>
          <span className={styles.avatarEmoji}>👩</span>
        </div>
        <div className={styles.profileInfo}>
          <h2 className={styles.playerName}>{name}</h2>
          <span className={styles.location}>📍 {location || 'Дома'}</span>
        </div>
      </div>

      {/* Main stats */}
      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>Основные показатели</h3>
        <div className={styles.statsList}>
          <StatKeyBar statKey="energy" value={stats.energy} showLabel />
          <StatKeyBar statKey="health" value={stats.health} showLabel />
          <StatKeyBar statKey="mood" value={stats.mood} showLabel />
          <StatKeyBar statKey="stress" value={stats.stress} showLabel />
          <StatKeyBar statKey="selfEsteem" value={stats.selfEsteem} showLabel />
        </div>
      </section>

      {/* Money */}
      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>Финансы</h3>
        <div className={styles.moneyDisplay}>
          <span className={styles.moneyLabel}>Деньги</span>
          <span className={styles.moneyValue}>₽ {stats.money.toLocaleString('ru-RU')}</span>
        </div>
      </section>

      {/* Job */}
      <section className={styles.section}>
        <h3 className={styles.sectionTitle}>Работа</h3>
        <div className={styles.jobStats}>
          <div className={styles.jobStat}>
            <span className={styles.jobStatLabel}>Удовлетворённость</span>
            <div className={styles.jobBar}>
              <div
                className={styles.jobBarFill}
                style={{ width: `${job.satisfaction}%`, background: jobSatisfactionColor }}
              />
            </div>
            <span className={styles.jobStatValue}>{Math.round(job.satisfaction)}%</span>
          </div>
          <div className={styles.jobStat}>
            <span className={styles.jobStatLabel}>Риск выгорания</span>
            <div className={styles.jobBar}>
              <div
                className={styles.jobBarFill}
                style={{ width: `${job.burnoutRisk}%`, background: burnoutColor }}
              />
            </div>
            <span className={styles.jobStatValue}>{Math.round(job.burnoutRisk)}%</span>
          </div>
        </div>
      </section>
    </div>
  );
}
