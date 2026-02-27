import { Heart, MessageCircle, Users } from 'lucide-react';
import { Card } from '../shared/Card';
import { useTelegram } from '../../hooks/useTelegram';
import type { NPC } from '../../types/game';
import '../../styles/components/NPCCard.css';

interface NPCCardProps {
  npc: NPC;
  onClick?: (npcId: string) => void;
}

const NPC_TYPE_LABELS: Record<NPC['type'], string> = {
  husband: '💑 Муж',
  father: '👨‍👧 Отец',
  friend: '👥 Друг',
};

const NPC_TYPE_ICONS: Record<NPC['type'], JSX.Element> = {
  husband: <Heart size={18} />,
  father: <Users size={18} />,
  friend: <MessageCircle size={18} />,
};

export function NPCCard({ npc, onClick }: NPCCardProps) {
  const { hapticFeedback } = useTelegram();
  const { id, name, relationship, avatarUrl, type } = npc;

  const getRelationshipColor = (value: number) => {
    if (value >= 70) return 'var(--color-success)';
    if (value >= 40) return 'var(--color-warning)';
    return 'var(--color-danger)';
  };

  const getRelationshipLabel = (value: number) => {
    if (value >= 80) return 'Отлично';
    if (value >= 60) return 'Хорошо';
    if (value >= 40) return 'Нормально';
    if (value >= 20) return 'Напряжённо';
    return 'Плохо';
  };

  const handleClick = () => {
    hapticFeedback?.impactOccurred('light');
    onClick?.(id);
  };

  return (
    <Card
      variant="elevated"
      padding="medium"
      onClick={handleClick}
      className="npc-card"
    >
      <div className="npc-card__header">
        <div className="npc-card__avatar">
          {avatarUrl ? (
            <img src={avatarUrl} alt={name} className="npc-card__avatar-image" />
          ) : (
            <div className="npc-card__avatar-placeholder">
              {NPC_TYPE_ICONS[type]}
            </div>
          )}
        </div>

        <div className="npc-card__info">
          <h3 className="npc-card__name">{name}</h3>
          <div className="npc-card__type">{NPC_TYPE_LABELS[type]}</div>
        </div>
      </div>

      <div className="npc-card__relationship">
        <div className="npc-card__relationship-header">
          <span className="npc-card__relationship-label">Отношения</span>
          <span 
            className="npc-card__relationship-value"
            style={{ color: getRelationshipColor(relationship) }}
          >
            {getRelationshipLabel(relationship)}
          </span>
        </div>
        
        <div className="npc-card__relationship-bar">
          <div
            className="npc-card__relationship-fill"
            style={{
              width: `${relationship}%`,
              backgroundColor: getRelationshipColor(relationship),
            }}
          />
        </div>
      </div>
    </Card>
  );
}
