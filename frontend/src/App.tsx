import { useEffect } from 'react';
import { GameScreen } from './components/game/GameScreen';
import { useContentStore } from './store/contentStore';

function App() {
  useEffect(() => {
    useContentStore.getState().loadAllContent();
  }, []);

  return <GameScreen />;
}

export default App;
