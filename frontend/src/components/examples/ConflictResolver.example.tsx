/**
 * Example: Data-driven Conflict Resolver
 * 
 * TEMPORARILY DISABLED - requires useGameState hook implementation
 * 
 * Shows how to render conflict resolution UI dynamically from ContentStore.
 * 
 * Key features:
 * - Tactics loaded from ConflictDef
 * - Skill requirements checked against player skills
 * - Success chance calculated from base + skill modifiers
 * - Locked tactics grayed out with tooltip
 */

/*
import { useContentStore } from '../../store/contentStore';
import { useGameState } from '../../hooks/useGameState';

interface ConflictResolverProps {
  conflictId: string;
  conflictType: string;
}

export function ConflictResolver({ conflictId, conflictType }: ConflictResolverProps) {
  const { conflicts } = useContentStore();
  const { gameState, resolveConflict } = useGameState();

  const conflictDef = conflicts[conflictType];

  if (!conflictDef || !gameState) {
    return <div>Загрузка конфликта...</div>;
  }

  const playerSkills = gameState.player.skills;

  return (
    <div className="conflict-modal">
      <div className="conflict-header">
        <h2>⚠️ {conflictDef.title}</h2>
        <p className="conflict-description">{conflictDef.description}</p>
        <div className="stress-indicator">
          🔥 Стресс: {conflictDef.baseStressPoints}
        </div>
      </div>

      <div className="tactics-list">
        <h3>Выберите тактику:</h3>
        
        {conflictDef.tactics.map(tactic => {
          const missingSkills = Object.entries(tactic.skillRequirements)
            .filter(([skill, minLevel]) => (playerSkills[skill] || 0) < minLevel);
          
          const isLocked = missingSkills.length > 0;
          const successChance = calculateSuccessChance(tactic, playerSkills);

          return (
            <TacticButton
              key={tactic.code}
              tactic={tactic}
              isLocked={isLocked}
              missingSkills={missingSkills}
              successChance={successChance}
              onClick={() => resolveConflict(conflictId, tactic.code)}
            />
          );
        })}
      </div>
    </div>
  );
}

function calculateSuccessChance(
  tactic: any,
  playerSkills: Record<string, number>
): number {
  let chance = tactic.baseSuccessChance;
  
  Object.entries(tactic.skillSuccessModifiers).forEach(([skill, modifier]) => {
    const skillLevel = playerSkills[skill] || 0;
    chance += (modifier as number) * (skillLevel / 100);
  });
  
  return Math.min(100, Math.max(0, chance));
}

interface TacticButtonProps {
  tactic: any;
  isLocked: boolean;
  missingSkills: [string, number][];
  successChance: number;
  onClick: () => void;
}

function TacticButton({
  tactic,
  isLocked,
  missingSkills,
  successChance,
  onClick,
}: TacticButtonProps) {
  return (
    <button
      className={`tactic-button ${isLocked ? 'locked' : ''}`}
      onClick={onClick}
      disabled={isLocked}
      title={
        isLocked
          ? `Требуется: ${missingSkills.map(([s, l]) => `${s} ${l}`).join(', ')}`
          : undefined
      }
    >
      {isLocked && <div className="lock-icon">🔒</div>}
      
      <div className="tactic-header">
        <h4>{tactic.title}</h4>
        <span className="success-chance">
          🎯 {successChance}%
        </span>
      </div>
      
      <p className="tactic-description">{tactic.description}</p>
      
      <div className="tactic-effects">
        {tactic.stressReduction !== 0 && (
          <span className={tactic.stressReduction > 0 ? 'positive' : 'negative'}>
            🔥 {tactic.stressReduction > 0 ? '-' : '+'}{Math.abs(tactic.stressReduction)} стресс
          </span>
        )}
        
        {Object.entries(tactic.relationshipEffects).map(([npc, value]) => (
          <span key={npc} className={(value as number) > 0 ? 'positive' : 'negative'}>
            👤 {npc}: {(value as number) > 0 ? '+' : ''}{value}
          </span>
        ))}
        
        {Object.entries(tactic.skillGains).map(([skill, gain]) => (
          <span key={skill} className="skill-gain">
            🎓 {skill}: +{gain}
          </span>
        ))}
      </div>
      
      {isLocked && (
        <div className="requirements">
          Требуется:
          {missingSkills.map(([skill, level]) => (
            <span key={skill}>{skill} {level}</span>
          ))}
        </div>
      )}
    </button>
  );
}
*/

// Placeholder export to prevent import errors
export {};
