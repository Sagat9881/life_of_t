import { Home, Zap, Heart } from 'lucide-react';
import { useHaptic } from '@/hooks/useHaptic';
import styles from './BottomNav.module.css';

export type NavItem = 'home' | 'actions' | 'relationships';

interface BottomNavProps {
  activeItem: NavItem;
  onNavigate: (item: NavItem) => void;
}

const navItems = [
  {
    id: 'home' as const,
    label: 'Главная',
    icon: Home,
  },
  {
    id: 'actions' as const,
    label: 'Действия',
    icon: Zap,
  },
  {
    id: 'relationships' as const,
    label: 'Отношения',
    icon: Heart,
  },
];

export const BottomNav = ({ activeItem, onNavigate }: BottomNavProps) => {
  const { selection } = useHaptic();

  const handleClick = (item: NavItem) => {
    if (item !== activeItem) {
      selection();
      onNavigate(item);
    }
  };

  return (
    <nav className={styles.nav}>
      {navItems.map((item) => {
        const Icon = item.icon;
        const isActive = item.id === activeItem;

        return (
          <button
            key={item.id}
            className={`${styles.item} ${isActive ? styles.active : ''}`}
            onClick={() => handleClick(item.id)}
            aria-label={item.label}
            aria-current={isActive ? 'page' : undefined}
          >
            <Icon className={styles.icon} size={24} />
            <span className={styles.label}>{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};
