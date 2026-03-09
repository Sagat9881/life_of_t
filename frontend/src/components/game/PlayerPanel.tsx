import { User } from 'lucide-react';
import { Card } from '../shared/Card';
import { StatBar } from '../shared/StatBar';
import type { Player } from '../../types/game';
import '../../styles/components/PlayerPanel.css';

interface PlayerPanelProps {
  player: Player;
  compact?: boolean;
}

export function PlayerPanel({ player, compact = false }: PlayerPanelProps) {
  const { name, stats } = player;

  return (
    <Card variant="elevated" padding={compact ? 'medium' : 'large'} className="player-panel">
      <div className="player-panel__header">
        <div className="player-panel__avatar">
          <div className="player-panel__avatar-placeholder">
            <User size={compact ? 32 : 48} />
          </div>
        </div>
        <div className="player-panel__info">
          <h2 className="player-panel__name">{name}</h2>
        </div>
      </div>
      <div className="player-panel__stats">
        <div className="player-panel__stat-group">
          <StatBar statKey="energy" value={stats.energy} showLabel size={compact ? 'small' : 'medium'} />
          <StatBar statKey="health" value={stats.health} showLabel size={compact ? 'small' : 'medium'} />
          <StatBar statKey="mood" value={stats.mood} showLabel size={compact ? 'small' : 'medium'} />
        </div>
        <div className="player-panel__stat-group">
          <StatBar statKey="stress" value={stats.stress} showLabel size={compact ? 'small' : 'medium'} />
          <StatBar statKey="selfEsteem" value={stats.selfEsteem} showLabel size={compact ? 'small' : 'medium'} />
          <StatBar statKey="money" value={stats.money} showLabel size={compact ? 'small' : 'medium'} />
        </div>
      </div>
    </Card>
  );
}
