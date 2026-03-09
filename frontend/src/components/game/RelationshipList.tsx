import { NPCCard } from './NPCCard';
import { PetCard } from './PetCard';
import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { NPC, PetView } from '../../types/game';
import '../../styles/components/RelationshipList.css';

interface RelationshipListProps {
  npcs: NPC[];
  pets: PetView[];
  isLoading?: boolean;
  error?: string | null;
  onNPCClick?: (npcId: string) => void;
  onPetClick?: (petId: string) => void;
  onRetry?: () => void;
}

export function RelationshipList({ npcs, pets, isLoading = false, error = null, onNPCClick, onPetClick, onRetry }: RelationshipListProps) {
  if (isLoading) {
    return <div className="relationship-list__loading"><LoadingSpinner size="large" text="Загрузка отношений..." /></div>;
  }
  if (error) {
    return <div className="relationship-list__error"><ErrorMessage message={error} {...(onRetry && { onRetry })} /></div>;
  }
  const hasContent = npcs.length > 0 || pets.length > 0;
  if (!hasContent) {
    return <div className="relationship-list__empty"><p>Нет доступных отношений</p></div>;
  }
  return (
    <div className="relationship-list">
      {npcs.length > 0 && (
        <section className="relationship-list__section">
          <h2 className="relationship-list__title">👥 Люди</h2>
          <div className="relationship-list__grid">
            {npcs.map(npc => <NPCCard key={npc.id} npc={npc} {...(onNPCClick && { onClick: onNPCClick })} />)}
          </div>
        </section>
      )}
      {pets.length > 0 && (
        <section className="relationship-list__section">
          <h2 className="relationship-list__title">🐾 Питомцы</h2>
          <div className="relationship-list__grid">
            {pets.map(pet => <PetCard key={pet.petId} pet={pet} {...(onPetClick && { onClick: onPetClick })} />)}
          </div>
        </section>
      )}
    </div>
  );
}
