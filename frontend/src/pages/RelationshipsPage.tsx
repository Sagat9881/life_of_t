import { useEffect } from 'react';
import { RelationshipList } from '../components/game/RelationshipList';
import { Card } from '../components/shared/Card';
import { useGameStore } from '../store/gameStore';
import '../styles/pages/RelationshipsPage.css';

export function RelationshipsPage() {
  const { npcs, pets, isLoading, error, fetchGameState } = useGameStore();
  useEffect(() => { fetchGameState(); }, [fetchGameState]);

  return (
    <div className="relationships-page">
      <Card variant="elevated" padding="large">
        <h1 className="relationships-page__title">Отношения</h1>
        <p className="relationships-page__subtitle">Управляйте отношениями с близкими людьми и питомцами</p>
        <RelationshipList
          npcs={npcs}
          pets={pets}
          isLoading={isLoading}
          error={error}
          onNPCClick={(id) => console.log('Open NPC dialog:', id)}
          onPetClick={(id) => console.log('Interact with pet:', id)}
          onRetry={fetchGameState}
        />
      </Card>
    </div>
  );
}
