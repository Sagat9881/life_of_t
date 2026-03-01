import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { RoomPage } from './pages/RoomPage';
import { BackgroundTest } from './pages/BackgroundTest';
import { HomePage } from './pages/HomePage';
import { ActionsPage } from './pages/ActionsPage';
import { RelationshipsPage } from './pages/RelationshipsPage';
import { ProfilePage } from './pages/ProfilePage';
import { OfficePage } from './pages/OfficePage';
import { ParkPage } from './pages/ParkPage';
import { StatsPage } from './pages/StatsPage';
import { PetsPage } from './pages/PetsPage';
import { QuestsPage } from './pages/QuestsPage';
import { EndingPage } from './pages/EndingPage';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <Routes>
          {/* Main pages */}
          <Route path="/" element={<Navigate to="/room" replace />} />
          <Route path="/home" element={<HomePage />} />
          
          {/* Primary Flow - Bottom Navigation */}
          <Route path="/room" element={<RoomPage />} />
          <Route path="/actions" element={<ActionsPage />} />
          <Route path="/relationships" element={<RelationshipsPage />} />
          <Route path="/stats" element={<StatsPage />} />
          
          {/* Locations */}
          <Route path="/office" element={<OfficePage />} />
          <Route path="/park" element={<ParkPage />} />
          
          {/* Additional screens */}
          <Route path="/pets" element={<PetsPage />} />
          <Route path="/quests" element={<QuestsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          
          {/* Game flow */}
          <Route path="/ending" element={<EndingPage />} />
          
          {/* Test pages */}
          <Route path="/test/backgrounds" element={<BackgroundTest />} />
          
          {/* Fallback */}
          <Route path="*" element={<Navigate to="/room" replace />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
