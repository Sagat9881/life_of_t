/**
 * HUD - Heads-Up Display component
 * Based on HUD.xml specification
 */

import React from 'react';
import { Battery, Smile, Heart, Clock, MapPin } from 'lucide-react';
import styles from './HUD.module.css';
import { useGameState } from '../../context/GameStateContext';

export const HUD: React.FC = () => {
  const { stats, time, money, location } = useGameState();

  const formatTime = (time: string) => {
    return time || '09:00';
  };

  const formatDate = (date: string, weekday: string) => {
    return `${date} • ${weekday}`;
  };

  const getStatColor = (value: number, max: number) => {
    const percent = (value / max) * 100;
    if (percent <= 30) return 'critical';
    if (percent <= 50) return 'warning';
    return 'normal';
  };

  return (
    <div className={styles.hud}>
      {/* Left section - Primary stats */}
      <div className={styles.leftSection}>
        <Stat
          icon={<Battery size={20} />}
          value={stats.energy}
          max={100}
          color="#FCD34D"
          label="Энергия"
          className={getStatColor(stats.energy, 100)}
        />
        <Stat
          icon={<Smile size={20} />}
          value={stats.happiness}
          max={100}
          color="#F093FB"
          label="Счастье"
          className={getStatColor(stats.happiness, 100)}
        />
        <Stat
          icon={<Heart size={20} />}
          value={stats.health}
          max={100}
          color="#EF4444"
          label="Здоровье"
          className={getStatColor(stats.health, 100)}
        />
      </div>

      {/* Center section - Time & Date */}
      <div className={styles.centerSection}>
        <div className={styles.timeRow}>
          <Clock size={18} className={styles.clockIcon} />
          <span className={styles.time}>{formatTime(time.current)}</span>
        </div>
        <div className={styles.dateRow}>
          {formatDate(time.date, time.weekday)}
        </div>
      </div>

      {/* Right section - Money & Location */}
      <div className={styles.rightSection}>
        <div className={styles.money}>
          <span className={styles.moneyEmoji}>💰</span>
          <span className={styles.moneyAmount}>{money}</span>
        </div>
        <div className={styles.location}>
          <MapPin size={18} />
          <span className={styles.locationName}>{location}</span>
        </div>
      </div>
    </div>
  );
};

interface StatProps {
  icon: React.ReactNode;
  value: number;
  max: number;
  color: string;
  label: string;
  className?: string;
}

const Stat: React.FC<StatProps> = ({ icon, value, max, color, label, className }) => {
  const percentage = (value / max) * 100;

  return (
    <div className={`${styles.stat} ${className ? styles[className] : ''}`} title={label}>
      <div className={styles.statIcon} style={{ color }}>
        {icon}
      </div>
      <div className={styles.statValue}>{value}</div>
      <div className={styles.statBar}>
        <div
          className={styles.statBarFill}
          style={{
            width: `${percentage}%`,
            background: color,
          }}
        />
      </div>
    </div>
  );
};