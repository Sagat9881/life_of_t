/**
 * App - Main application component with HUD, Navigation, and Scene
 */

import { useState } from 'react';
import { GameStateProvider } from './context/GameStateContext';
import { HUD } from './components/ui/HUD';
import { BottomNavigation, NavTab } from './components/ui/BottomNavigation';
import { RoomScreen } from './components/scene/RoomScreen';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState<NavTab>('home');
  const [availableActions] = useState(3);
  const [hasRelEvents] = useState(true);

  const handleObjectTap = (objectId: string) => {
    console.log('Object tapped:', objectId);
    // TODO: Open ActionMenu modal
  };

  const renderContent = () => {
    switch (activeTab) {
      case 'home':
        return <RoomScreen gameState={{}} onObjectTap={handleObjectTap} />;
      case 'actions':
        return (
          <div style={{ 
            position: 'fixed',
            top: '80px',
            left: 0,
            right: 0,
            bottom: '64px',
            padding: '20px',
            overflow: 'auto',
            background: '#F9FAFB'
          }}>
            <h2>Действия</h2>
            <p>ActionsPage будет здесь</p>
          </div>
        );
      case 'relationships':
        return (
          <div style={{ 
            position: 'fixed',
            top: '80px',
            left: 0,
            right: 0,
            bottom: '64px',
            padding: '20px',
            overflow: 'auto',
            background: '#F9FAFB'
          }}>
            <h2>Отношения</h2>
            <p>RelationshipsPage будет здесь</p>
          </div>
        );
      case 'stats':
        return (
          <div style={{ 
            position: 'fixed',
            top: '80px',
            left: 0,
            right: 0,
            bottom: '64px',
            padding: '20px',
            overflow: 'auto',
            background: '#F9FAFB'
          }}>
            <h2>Статистика</h2>
            <p>StatsPage будет здесь</p>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <GameStateProvider>
      <div className="app">
        <HUD />
        {renderContent()}
        <BottomNavigation
          activeTab={activeTab}
          onTabChange={setActiveTab}
          availableActionsCount={availableActions}
          hasRelationshipEvents={hasRelEvents}
        />
      </div>
    </GameStateProvider>
  );
}

export default App;