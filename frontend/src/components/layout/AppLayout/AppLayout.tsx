import { type ReactNode } from 'react';
import { BottomNav, type NavItem } from '../BottomNav';
import styles from './AppLayout.module.css';

interface AppLayoutProps {
  children: ReactNode;
  currentNav: NavItem;
  onNavigate: (item: NavItem) => void;
  showNav?: boolean;
}

export const AppLayout = ({
  children,
  currentNav,
  onNavigate,
  showNav = true,
}: AppLayoutProps) => {
  return (
    <div className={styles.layout}>
      <main className={styles.main}>{children}</main>
      {showNav && <BottomNav activeItem={currentNav} onNavigate={onNavigate} />}
    </div>
  );
};
