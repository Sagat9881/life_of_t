import { LoadingSpinner } from '../shared/LoadingSpinner';
import { ErrorMessage } from '../shared/ErrorMessage';
import type { RelationshipView, PetView } from '../../types/game';
import '../../styles/components/RelationshipList.css';

interface RelationshipListProps {
  relationships: RelationshipView[];
  pets:          PetView[];
  isLoading?:    boolean;
  error?:        string | null;
  onNPCClick?:   (npcId: string) => void;
  onPetClick?:   (petId: string) => void;
  onRetry?:      () => void;
}

export function RelationshipList({
  relationships,
  pets,
  isLoading = false,
  error = null,
  onNPCClick,
  onPetClick,
  onRetry,
}: RelationshipListProps) {
  if (isLoading) {
    return <div className="relationship-list__loading"><LoadingSpinner size="large" text="Загрузка отношений..." /></div>;
  }
  if (error) {
    return <div className="relationship-list__error"><ErrorMessage message={error} {...(onRetry && { onRetry })} /></div>;
  }
  if (relationships.length === 0 && pets.length === 0) {
    return <div className="relationship-list__empty"><p>Нет доступных отношений</p></div>;
  }

  return (
    <div className="relationship-list">
      {relationships.length > 0 && (
        <section className="relationship-list__section">
          <h2 className="relationship-list__title">👥 Люди</h2>
          <div className="relationship-list__grid">
            {relationships.map((rel) => (
              <div
                key={rel.npcId}
                className="relationship-list__card"
                onClick={() => onNPCClick?.(rel.npcId)}
                style={{ cursor: onNPCClick ? 'pointer' : 'default' }}
              >
                <div className="relationship-list__name">{rel.name}</div>
                <div className="relationship-list__stats">
                  <span>❤️ {rel.closeness}</span>
                  <span>🤝 {rel.trust}</span>
                  <span>💞 {rel.romance}</span>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
      {pets.length > 0 && (
        <section className="relationship-list__section">
          <h2 className="relationship-list__title">🐾 Питомцы</h2>
          <div className="relationship-list__grid">
            {pets.map((pet) => (
              <div
                key={pet.petId}
                className="relationship-list__card"
                onClick={() => onPetClick?.(pet.petId)}
                style={{ cursor: onPetClick ? 'pointer' : 'default' }}
              >
                <div className="relationship-list__name">{pet.name}</div>
                <div className="relationship-list__stats">
                  <span>🍖 {pet.satiety}</span>
                  <span>💛 {pet.attention}</span>
                  <span>❤️ {pet.health}</span>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
