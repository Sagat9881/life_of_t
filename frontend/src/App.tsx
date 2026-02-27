import { useState } from 'react';
import { AppLayout } from './components/layout/AppLayout';
import { HomePage } from './pages/HomePage';
import { RelationshipsPage } from './pages/RelationshipsPage';
import { ProfilePage } from './pages/ProfilePage';
import type { NavItem } from './components/layout/BottomNav';
import './styles/globals.css';

function App() {
  const [currentPage, setCurrentPage] = useState<NavItem>('home');

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <HomePage />;
      case 'relationships':
        return <RelationshipsPage />;
      case 'profile':
        return <ProfilePage />;
      default:
        return <HomePage />;
    }
  };

  return (
    <AppLayout currentNav={currentPage} onNavigate={setCurrentPage}>
      {renderPage()}
    </AppLayout>
  );
}

export default App;
