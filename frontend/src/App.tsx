import { Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './components/shared/AppLayout';
import { RoomPage } from './pages/RoomPage';
import { StatsPage } from './pages/StatsPage';
import { ActionsPage } from './pages/ActionsPage';
import { RelationshipsPage } from './pages/RelationshipsPage';
import { ProfilePage } from './pages/ProfilePage';
import { EndingPage } from './pages/EndingPage';
import { useGameStore } from './store/gameStore';

function App() {
  const ending = useGameStore(s => s.state?.ending);

  if (ending) {
    return <EndingPage ending={ending} />;
  }

  return (
    <Routes>
      <Route path="/" element={<AppLayout />}>
        <Route index element={<Navigate to="/room" replace />} />
        <Route path="room" element={<RoomPage />} />
        <Route path="stats" element={<StatsPage />} />
        <Route path="actions" element={<ActionsPage />} />
        <Route path="relationships" element={<RelationshipsPage />} />
        <Route path="profile" element={<ProfilePage />} />
      </Route>
    </Routes>
  );
}

export default App;
