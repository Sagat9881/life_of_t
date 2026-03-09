/**
 * Example: Data-driven Actions List
 * 
 * This component demonstrates how to use ContentStore to render
 * action buttons WITHOUT hardcoding any game data in UI code.
 * 
 * Key points:
 * - GameStateView provides available action CODES
 * - ContentStore provides METADATA (title, icon, description)
 * - UI is fully data-driven: add new action in XML → appears automatically
 */

import React from 'react';
import { useContentStore } from '../../store/contentStore';
import { useGameState } from '../../hooks/useGameState';

export function ActionsList() {
  const { actions } = useContentStore();
  const { gameState, executeAction } = useGameState();

  if (!gameState) {
    return <div>Загрузка...</div>;
  }

  return (
    <div className="actions-list">
      <h2>Доступные действия</h2>
      
      {gameState.availableActions.map(option => {
        // Look up action metadata from ContentStore
        const actionDef = actions[option.actionCode];
        
        if (!actionDef) {
          console.warn(`Unknown action: ${option.actionCode}`);
          return null;
        }

        // Check if player has enough energy
        const canAfford = gameState.player.stats.energy >= actionDef.minEnergy;
        
        // Check skill requirements
        const hasSkills = Object.entries(actionDef.requiredSkills).every(
          ([skill, minLevel]) => (gameState.player.skills[skill] || 0) >= minLevel
        );

        const isDisabled = !canAfford || !hasSkills;

        return (
          <ActionButton
            key={option.actionCode}
            code={option.actionCode}
            title={actionDef.title}
            description={actionDef.description}
            icon={actionDef.iconName}
            energyCost={actionDef.energyCost}
            duration={actionDef.durationMinutes}
            effects={actionDef.statEffects}
            disabled={isDisabled}
            disabledReason={
              !canAfford
                ? `Нужно энергии: ${actionDef.minEnergy}`
                : !hasSkills
                ? 'Недостаточно навыков'
                : undefined
            }
            onClick={() => executeAction(option.actionCode)}
          />
        );
      })}
    </div>
  );
}

// ==================== Action Button Component ====================

interface ActionButtonProps {
  code: string;
  title: string;
  description: string;
  icon: string;
  energyCost: number;
  duration: number;
  effects: Record<string, number>;
  disabled: boolean;
  disabledReason?: string;
  onClick: () => void;
}

function ActionButton({
  title,
  description,
  icon,
  energyCost,
  duration,
  effects,
  disabled,
  disabledReason,
  onClick,
}: ActionButtonProps) {
  return (
    <button
      className={`action-button ${disabled ? 'disabled' : ''}`}
      onClick={onClick}
      disabled={disabled}
      title={disabledReason}
    >
      <div className="action-icon">
        <i className={`icon-${icon}`} />
      </div>
      
      <div className="action-info">
        <h3>{title}</h3>
        <p>{description}</p>
        
        <div className="action-meta">
          <span className="energy-cost">⚡ -{energyCost}</span>
          <span className="duration">⏱️ {Math.floor(duration / 60)}ч</span>
          
          {Object.entries(effects).map(([stat, value]) => (
            <span key={stat} className={`effect ${value > 0 ? 'positive' : 'negative'}`}>
              {stat}: {value > 0 ? '+' : ''}{value}
            </span>
          ))}
        </div>
      </div>
    </button>
  );
}
