import { useState } from 'react';
import { AppLayout } from './components/layout/AppLayout';
import { RoomPage } from './pages/RoomPage';
import { RelationshipsPage } from './pages/RelationshipsPage';
import { ProfilePage } from './pages/ProfilePage';
import { ShutdownButton } from './components/demo/ShutdownButton';
import type { NavItem } from './components/layout/BottomNav';
import './styles/globals.css';

function App() {
  const [currentPage, setCurrentPage] = useState<NavItem>('home');

  const renderPage = () => {
    switch (currentPage) {
      case 'home':
        return <RoomPage />;
      case 'relationships':
        return <RelationshipsPage />;
      case 'profile':
        return <ProfilePage />;
      default:
        return <RoomPage />;
    }
  };

  return (
    <>
      <AppLayout currentNav={currentPage} onNavigate={setCurrentPage}>
        {renderPage()}
      </AppLayout>
      {/* Кнопка выключения демо */}
      <ShutdownButton />
    </>
  );
}

export default App;
