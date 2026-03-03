import { Home, Zap, Users, BarChart3 } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import styles from './BottomNav.module.css';

const NAV_ITEMS = [
  { path: '/room',          icon: Home,     label: 'Дом'       },
  { path: '/actions',       icon: Zap,      label: 'Действия' },
  { path: '/relationships', icon: Users,    label: 'Люди'     },
  { path: '/stats',         icon: BarChart3, label: 'Статс'     },
];

export function BottomNav() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <nav className={styles.nav}>
      <div className={styles.inner}>
        {NAV_ITEMS.map(({ path, icon: Icon, label }) => {
          const isActive = location.pathname.startsWith(path);
          return (
            <button
              key={path}
              className={`${styles.item} ${isActive ? styles.active : ''}`}
              onClick={() => navigate(path)}
              aria-label={label}
            >
              <Icon size={20} className={styles.icon} />
              <span className={styles
.label}>{label}</span>
            </button>
          );
        })}
      </div>
    </nav>
  );
}
