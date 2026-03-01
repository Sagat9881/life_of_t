import { BottomNav } from '../BottomNav';
import './AppLayout.css';

interface AppLayoutProps {
  children: React.ReactNode;
}

export function AppLayout({ children }: AppLayoutProps) {
  return (
    <div className="app-layout">
      <main className="app-layout__content">
        {children}
      </main>
      <BottomNav />
    </div>
  );
}
