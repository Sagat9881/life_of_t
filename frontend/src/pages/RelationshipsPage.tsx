import { useGameStore } from '@/store/gameStore';
import { RelationshipCard } from '@/components/relationships/RelationshipCard';
import { PetCard } from '@/components/relationships/PetCard';
import { LoadingSpinner } from '@/components/shared/LoadingSpinner';
import styles from './RelationshipsPage.module.css';

export function RelationshipsPage() {
  const { gameState, isLoading } = useGameStore();

  const relationships = gameState?.relationships ?? [];
  const pets = gameState?.pets ?? [];

  if (isLoading && !gameState) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner size="lg" text="Загрузка..." />
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <h1 className={styles.title}>❤️ Отношения</h1>
      </div>

      {relationships.length > 0 && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>Люди</h2>
          <div className={styles.grid}>
            {relationships.map(rel => (
              <RelationshipCard key={rel.npcCode} relationship={rel} />
            ))}
          </div>
        </section>
      )}

      {pets.length > 0 && (
        <section className={styles.section}>
          <h2 className={styles.sectionTitle}>🐾 Питомцы</h2>
          <div className={styles.grid}>
            {pets.map(pet => (
              <PetCard key={pet.id} pet={pet} />
            ))}
          </div>
        </section>
      )}

      {relationships.length === 0 && pets.length === 0 && (
        <div className={styles.empty}>
          <p>Отношения появятся после начала игры</p>
        </div>
      )}
    </div>
  );
}
