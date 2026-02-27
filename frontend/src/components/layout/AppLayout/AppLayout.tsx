import { BottomNav, type NavItem } from '../BottomNav';
import './AppLayout.css';

interface AppLayoutProps {
  children: React.ReactNode;
  currentNav: NavItem;
  onNavigate: (item: NavItem) => void;
}

export function AppLayout({ children, currentNav, onNavigate }: AppLayoutProps) {
  return (
    <div className="app-layout">
      <main className="app-layout__content">
        {children}
      </main>
      <BottomNav current={currentNav} onNavigate={onNavigate} />
    </div>
  );
}
