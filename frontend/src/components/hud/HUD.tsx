import { Moon, Sun } from 'lucide-react';
import { useGameStore } from '@/store/gameStore';
import styles from './HUD.module.css';

function getTimeOfDay(hour: number): 'morning' | 'day' | 'evening' | 'night' {
  if (hour >= 6 && hour < 12) return 'morning';
  if (hour >= 12 && hour < 18) return 'day';
  if (hour >= 18 && hour < 22) return 'evening';
  return 'night';
}

function formatHour(hour: number): string {
  return `${String(hour).padStart(2, '0')}:00`;
}

export function HUD() {
  const { gameState, isActionLoading, endDay } = useGameStore();

  if (!gameState) {
    return (
      <header className={styles.hud}>
        <div className={styles.loading}>Загрузка...</div>
      </header>
    );
  }

  const { player, time } = gameState;
  const { stats } = player;
  const tod = getTimeOfDay(time.hour);

  const energyPct = Math.max(0, Math.min(100, stats.energy));
  const healthPct = Math.max(0, Math.min(100, stats.health));
  const stressPct = Math.max(0, Math.min(100, stats.stress));
  const moodPct = Math.max(0, Math.min(100, stats.mood));

  return (
    <header className={`${styles.hud} ${styles[tod]}`}>
      <div className={styles.inner}>
        {/* Left: stats bars */}
        <div className={styles.stats}>
          <div className={styles.statRow} title={`Энергия: ${stats.energy}`}>
            <span className={styles.statIcon}>⚡</span>
            <div className={styles.bar}>
              <div className={`${styles.fill} ${styles.energy}`} style={{ width: `${energyPct}%` }} />
            </div>
          </div>
          <div className={styles.statRow} title={`Здоровье: ${stats.health}`}>
            <span className={styles.statIcon}>❤️</span>
            <div className={styles.bar}>
              <div className={`${styles.fill} ${styles.health}`} style={{ width: `${healthPct}%` }} />
            </div>
          </div>
          <div className={styles.statRow} title={`Стресс: ${stats.stress}`}>
            <span className={styles.statIcon}>😤</span>
            <div className={styles.bar}>
              <div className={`${styles.fill} ${styles.stress}`} style={{ width: `${stressPct}%` }} />
            </div>
          </div>
          <div className={styles.statRow} title={`Настроение: ${stats.mood}`}>
            <span className={styles.statIcon}>😊</span>
            <div className={styles.bar}>
              <div className={`${styles.fill} ${styles.mood}`} style={{ width: `${moodPct}%` }} />
            </div>
          </div>
        </div>

        {/* Center: money */}
        <div className={styles.money}>
          <span className={styles.moneyCurrency}>₽</span>
          <span className={styles.moneyAmount}>{stats.money.toLocaleString('ru-RU')}</span>
        </div>

        {/* Right: time + end day */}
        <div className={styles.timeSection}>
          <div className={styles.timeDisplay}>
            {tod === 'night' ? <Moon size={12} /> : <Sun size={12} />}
            <span className={styles.day}>День {time.day}</span>
            <span className={styles.hour}>{formatHour(time.hour)}</span>
          </div>
          <button
            className={styles.endDayBtn}
            onClick={endDay}
            disabled={isActionLoading}
            title="Завершить день"
          >
            <span className={styles.endDayText}>🌙 Спать</span>
          </button>
        </div>
      </div>
    </header>
  );
}
