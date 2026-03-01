import { Home, Zap, Users, BarChart3 } from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTelegram } from '../../../hooks/useTelegram';
import './BottomNav.css';

export type NavItem = 'room' | 'actions' | 'relationships' | 'stats';

interface BottomNavProps {
  current?: NavItem;
}

const NAV_ITEMS = [
  { id: 'room' as NavItem, icon: Home, label: 'Комната', path: '/room' },
  { id: 'actions' as NavItem, icon: Zap, label: 'Действия', path: '/actions' },
  { id: 'relationships' as NavItem, icon: Users, label: 'Отношения', path: '/relationships' },
  { id: 'stats' as NavItem, icon: BarChart3, label: 'Статы', path: '/stats' },
] as const;

export function BottomNav({ current }: BottomNavProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { hapticFeedback } = useTelegram();

  // Auto-detect current page from location if not provided
  const currentPage = current || NAV_ITEMS.find(item => location.pathname === item.path)?.id || 'room';

  const handleClick = (item: NavItem, path: string) => {
    if (item !== currentPage) {
      hapticFeedback?.selectionChanged();
      navigate(path);
    }
  };

  return (
    <nav className="bottom-nav">
      <div className="bottom-nav__container">
        {NAV_ITEMS.map(({ id, icon: Icon, label, path }) => (
          <button
            key={id}
            onClick={() => handleClick(id, path)}
            className={`bottom-nav__item ${
              currentPage === id ? 'bottom-nav__item--active' : ''
            }`}
            aria-label={label}
            aria-current={currentPage === id ? 'page' : undefined}
          >
            <Icon size={24} className="bottom-nav__icon" />
            <span className="bottom-nav__label">{label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}
