import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { RoomPage } from './pages/RoomPage';
import { BackgroundTest } from './pages/BackgroundTest';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <Routes>
          <Route path="/" element={<RoomPage />} />
          <Route path="/room" element={<RoomPage />} />
          <Route path="/test/backgrounds" element={<BackgroundTest />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
