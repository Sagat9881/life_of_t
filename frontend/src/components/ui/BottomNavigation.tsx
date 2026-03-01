/**
 * BottomNavigation - Main app navigation bar
 * Based on BottomNavigation.xml specification
 */

import React from 'react';
import { Home, ListChecks, HeartHandshake, BarChart3 } from 'lucide-react';
import styles from './BottomNavigation.module.css';
import { useHaptic } from '../../hooks/useHaptic';

export type NavTab = 'home' | 'actions' | 'relationships' | 'stats';

interface BottomNavigationProps {
  activeTab: NavTab;
  onTabChange: (tab: NavTab) => void;
  availableActionsCount?: number;
  hasRelationshipEvents?: boolean;
}

export const BottomNavigation: React.FC<BottomNavigationProps> = ({
  activeTab,
  onTabChange,
  availableActionsCount = 0,
  hasRelationshipEvents = false,
}) => {
  const haptic = useHaptic();

  const handleTabClick = (tab: NavTab) => {
    if (tab === activeTab) {
      haptic.selectionChanged();
    } else {
      haptic.light();
      onTabChange(tab);
    }
  };

  const tabs = [
    {
      id: 'home' as NavTab,
      icon: Home,
      label: 'Дом',
      tooltip: 'Вернуться домой',
    },
    {
      id: 'actions' as NavTab,
      icon: ListChecks,
      label: 'Действия',
      tooltip: 'Доступные действия',
      badge: availableActionsCount > 0 ? availableActionsCount : undefined,
    },
    {
      id: 'relationships' as NavTab,
      icon: HeartHandshake,
      label: 'Связи',
      tooltip: 'Отношения с людьми и питомцами',
      hasDot: hasRelationshipEvents,
    },
    {
      id: 'stats' as NavTab,
      icon: BarChart3,
      label: 'Статы',
      tooltip: 'Характеристики и навыки',
    },
  ];

  return (
    <nav className={styles.bottomNav}>
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.id;

        return (
          <button
            key={tab.id}
            className={`${styles.navItem} ${isActive ? styles.active : ''}`}
            onClick={() => handleTabClick(tab.id)}
            title={tab.tooltip}
            type="button"
          >
            <div className={styles.iconContainer}>
              <Icon size={24} className={styles.icon} />
              {tab.badge && (
                <span className={styles.badge}>{tab.badge}</span>
              )}
              {tab.hasDot && <span className={styles.dot} />}
            </div>
            <span className={styles.label}>{tab.label}</span>
            {isActive && <div className={styles.activeIndicator} />}
          </button>
        );
      })}
    </nav>
  );
};