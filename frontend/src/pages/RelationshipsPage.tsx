import { useGameStore } from '../store/gameStore';
import { RelationshipList } from '../components/game/RelationshipList';
import { BottomNav } from '../components/layout/BottomNav/BottomNav';
import './RelationshipsPage.css';

export function RelationshipsPage() {
  const { npcs, pets } = useGameStore();

  const handleNPCClick = (npcId: string) => {
    console.log('NPC clicked:', npcId);
    // TODO: открыть диалог с NPC
  };

  const handlePetClick = (petId: string) => {
    console.log('Pet clicked:', petId);
    // TODO: взаимодействие с питомцем
  };

  return (
    <div className="relationships-page">
      <div className="relationships-page__content">
        <header className="relationships-page__header">
          <h1 className="relationships-page__title">❤️ Отношения</h1>
          <p className="relationships-page__subtitle">
            Ваши связи с близкими людьми и питомцами
          </p>
        </header>

        <div className="relationships-page__list">
          <RelationshipList
            npcs={npcs}
            pets={pets}
            onNPCClick={handleNPCClick}
            onPetClick={handlePetClick}
          />
        </div>
      </div>

      <BottomNav current="relationships" />
    </div>
  );
}
