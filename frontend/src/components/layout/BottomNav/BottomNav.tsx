import { Home, Users, User } from 'lucide-react';
import { useTelegram } from '../../../hooks/useTelegram';
import './BottomNav.css';

export type NavItem = 'home' | 'relationships' | 'profile';

interface BottomNavProps {
  current: NavItem;
  onNavigate: (item: NavItem) => void;
}

const NAV_ITEMS = [
  { id: 'home' as NavItem, icon: Home, label: 'Главная' },
  { id: 'relationships' as NavItem, icon: Users, label: 'Отношения' },
  { id: 'profile' as NavItem, icon: User, label: 'Профиль' },
] as const;

export function BottomNav({ current, onNavigate }: BottomNavProps) {
  const { hapticFeedback } = useTelegram();

  const handleClick = (item: NavItem) => {
    if (item !== current) {
      hapticFeedback?.selectionChanged();
      onNavigate(item);
    }
  };

  return (
    <nav className="bottom-nav">
      <div className="bottom-nav__container">
        {NAV_ITEMS.map(({ id, icon: Icon, label }) => (
          <button
            key={id}
            onClick={() => handleClick(id)}
            className={`bottom-nav__item ${
              current === id ? 'bottom-nav__item--active' : ''
            }`}
            aria-label={label}
            aria-current={current === id ? 'page' : undefined}
          >
            <Icon size={24} className="bottom-nav__icon" />
            <span className="bottom-nav__label">{label}</span>
          </button>
        ))}
      </div>
    </nav>
  );
}
