import { useState } from 'react';
import { Button } from './components/shared/Button';
import { Card } from './components/shared/Card';
import { StatBar } from './components/shared/StatBar';
import { LoadingSpinner } from './components/shared/LoadingSpinner';
import { ErrorMessage } from './components/shared/ErrorMessage';
import { PlayerPanel } from './components/game/PlayerPanel';
import { ActionList } from './components/game/ActionList';
import { RelationshipList } from './components/game/RelationshipList';
import { ConflictResolver } from './components/game/ConflictResolver';
import { EventChoice } from './components/game/EventChoice';
import type { Stats, Player, GameAction, NPC, Pet, Conflict, GameEvent } from './types/game';
import { Power } from 'lucide-react';

function ComponentTest() {
  const [isLoading, setIsLoading] = useState(false);
  const [showError, setShowError] = useState(false);
  const [isShuttingDown, setIsShuttingDown] = useState(false);

  const mockStats: Stats = {
    energy: 75,
    health: 60,
    stress: 40,
    mood: 80,
    money: 1500,
    selfEsteem: 70,
  };

  const mockPlayer: Player = {
    id: 'demo-player',
    name: 'Таня',
    level: 5,
    stats: mockStats,
    avatarUrl: '',
  };

  const mockActions: GameAction[] = [
    {
      code: 'work_design',
      name: '💼 Работа (Дизайн)',
      description: 'Поработать над проектом на Tilda',
      timeCost: 2,
      energyCost: 20,
      effects: { energy: -20, money: 500, stress: 10 },
      available: true,
      category: 'Работа',
    },
    {
      code: 'rest_tv',
      name: '📺 Смотреть сериал',
      description: 'Расслабиться перед телевизором',
      timeCost: 1,
      effects: { energy: 10, mood: 15, stress: -10 },
      available: true,
      category: 'Отдых',
    },
    {
      code: 'social_husband',
      name: '💑 Время с мужем',
      description: 'Провести время вместе',
      timeCost: 2,
      effects: { mood: 20, stress: -15 },
      available: true,
      category: 'Отношения',
    },
    {
      code: 'hobby_reading',
      name: '📚 Почитать книгу',
      description: 'Погрузиться в увлекательный роман',
      timeCost: 1,
      effects: { mood: 10, stress: -5, selfEsteem: 5 },
      available: true,
      category: 'Хобби',
    },
    {
      code: 'pet_garfield',
      name: '🐱 Поиграть с Гарфилдом',
      description: 'Уделить время любимому коту',
      timeCost: 1,
      effects: { mood: 15, stress: -10 },
      available: true,
      category: 'Питомцы',
    },
    {
      code: 'sleep',
      name: '😴 Спать',
      description: 'Восстановить силы',
      timeCost: 8,
      effects: { energy: 100, health: 20, stress: -20 },
      available: false,
      category: 'Базовое',
    },
  ];

  const mockNPCs: NPC[] = [
    {
      id: 'npc-husband',
      name: 'Александр',
      type: 'husband',
      relationship: 85,
      avatarUrl: '',
    },
    {
      id: 'npc-father',
      name: 'Папа',
      type: 'father',
      relationship: 65,
      avatarUrl: '',
    },
  ];

  const mockPets: Pet[] = [
    {
      id: 'pet-garfield',
      name: 'Гарфилд',
      type: 'cat',
      species: 'Cat',
      mood: 90,
      hunger: 30,
      avatarUrl: '',
    },
    {
      id: 'pet-sam',
      name: 'Сэм',
      type: 'dog',
      species: 'Dog',
      mood: 75,
      hunger: 50,
      avatarUrl: '',
    },
  ];

  const mockConflict: Conflict = {
    id: 'conflict-1',
    description: 'Муж недоволен тем, что вы слишком много времени проводите за работой и мало внимания уделяете семье.',
    csp: 65,
    maxCSP: 100,
    tactics: [
      {
        code: 'apologize',
        name: '🙏 Извиниться',
        description: 'Признать свою ошибку и попросить прощения',
        successChance: 70,
      },
      {
        code: 'explain',
        name: '💬 Объяснить',
        description: 'Спокойно объяснить причины своего поведения',
        successChance: 55,
      },
      {
        code: 'compromise',
        name: '🤝 Найти компромисс',
        description: 'Предложить решение, устраивающее обоих',
        successChance: 80,
      },
      {
        code: 'ignore',
        name: '🚺 Игнорировать',
        description: 'Сделать вид, что ничего не произошло',
        successChance: 20,
      },
    ],
  };

  const mockEvent: GameEvent = {
    id: 'event-1',
    title: '🎁 Неожиданный подарок',
    description: 'Во время прогулки с Гарфилдом вы нашли коробку с котятами у подъезда. Они выглядят голодными и напуганными. Что вы сделаете?',
    choices: [
      {
        code: 'take_home',
        text: 'Забрать домой всех котят',
        consequences: '+20 настроение, +15 самооценка, -500 деньги (корм и ветеринар)',
      },
      {
        code: 'call_shelter',
        text: 'Позвонить в приют',
        consequences: '+10 настроение, +5 самооценка',
      },
      {
        code: 'ask_neighbors',
        text: 'Спросить у соседей',
        consequences: '+5 настроение, +10 отношения с соседями',
      },
      {
        code: 'ignore',
        text: 'Пройти мимо',
        consequences: '-10 настроение, -5 самооценка',
      },
    ],
  };

  const handleButtonClick = () => {
    setIsLoading(true);
    setTimeout(() => setIsLoading(false), 2000);
  };

  const handleActionExecute = (actionCode: string) => {
    console.log('Execute action:', actionCode);
    alert(`Выполнено действие: ${actionCode}`);
  };

  const handleNPCClick = (npcId: string) => {
    console.log('NPC clicked:', npcId);
    alert(`Открыть диалог с: ${npcId}`);
  };

  const handlePetClick = (petId: string) => {
    console.log('Pet clicked:', petId);
    alert(`Взаимодействие с питомцем: ${petId}`);
  };

  const handleTacticSelect = (tacticCode: string) => {
    console.log('Tactic selected:', tacticCode);
    alert(`Выбрана тактика: ${tacticCode}`);
  };

  const handleChoiceSelect = (choiceCode: string) => {
    console.log('Choice selected:', choiceCode);
    alert(`Выбран вариант: ${choiceCode}`);
  };

  const handleShutdown = async () => {
    if (!confirm('Выключить демо-приложение?')) {
      return;
    }

    setIsShuttingDown(true);
    try {
      await fetch('/api/shutdown', { method: 'POST' });
      alert('Приложение выключается... Окно можно закрыть.');
    } catch (error) {
      console.error('Ошибка при выключении:', error);
      setIsShuttingDown(false);
      alert('Не удалось выключить приложение. Используйте Ctrl+C в консоли.');
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '1rem' }}>
      <h1 style={{ fontFamily: 'Comfortaa, sans-serif', color: '#FF6B9D' }}>
        🎮 Component Test
      </h1>

      {/* EventChoice Demo */}
      <Card variant="elevated" padding="large">
        <h2>EventChoice</h2>
        <p style={{ marginBottom: '1rem', color: '#666' }}>
          Компонент для выбора варианта действия в событиях
        </p>
        <EventChoice
          event={mockEvent}
          onSelectChoice={handleChoiceSelect}
          onCancel={() => alert('Событие отменено')}
        />
      </Card>

      {/* ConflictResolver Demo */}
      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>ConflictResolver</h2>
        <p style={{ marginBottom: '1rem', color: '#666' }}>
          Компонент для разрешения конфликтов с CSP-шкалой и выбором тактик
        </p>
        <ConflictResolver
          conflict={mockConflict}
          onSelectTactic={handleTacticSelect}
          onCancel={() => alert('Конфликт отменён')}
        />
      </Card>

      {/* RelationshipList Demo */}
      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>RelationshipList</h2>
        <p style={{ marginBottom: '1rem', color: '#666' }}>
          Список персонажей и питомцев с показателями отношений
        </p>
        <RelationshipList 
          npcs={mockNPCs}
          pets={mockPets}
          onNPCClick={handleNPCClick}
          onPetClick={handlePetClick}
        />
      </Card>

      {/* ActionList Demo */}
      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>ActionList</h2>
        <p style={{ marginBottom: '1rem', color: '#666' }}>
          Список действий с поиском и фильтрацией по категориям
        </p>
        <ActionList 
          actions={mockActions}
          onExecuteAction={handleActionExecute}
        />
      </Card>

      {/* PlayerPanel Demo */}
      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>PlayerPanel</h2>
        <p style={{ marginBottom: '1rem', color: '#666' }}>
          Панель игрока с аватаром, именем, уровнем и статами
        </p>
        <PlayerPanel player={mockPlayer} />
        
        <div style={{ marginTop: '1.5rem' }}>
          <h3 style={{ marginBottom: '0.5rem' }}>Компактная версия:</h3>
          <PlayerPanel player={mockPlayer} compact />
        </div>
      </Card>

      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>Buttons</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <Button variant="primary" onClick={handleButtonClick}>
            Primary Button
          </Button>
          <Button variant="secondary" onClick={handleButtonClick}>
            Secondary Button
          </Button>
          <Button variant="accent" onClick={handleButtonClick}>
            Accent Button
          </Button>
          <Button variant="outline" onClick={handleButtonClick}>
            Outline Button
          </Button>
          <Button variant="primary" size="small">
            Small
          </Button>
          <Button variant="primary" size="large">
            Large
          </Button>
          <Button variant="primary" isLoading={isLoading}>
            Loading Button
          </Button>
          <Button variant="primary" disabled>
            Disabled Button
          </Button>
        </div>
      </Card>

      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>StatBars</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <StatBar statKey="energy" value={mockStats.energy} showLabel />
          <StatBar statKey="health" value={mockStats.health} showLabel />
          <StatBar statKey="stress" value={mockStats.stress} showLabel />
          <StatBar statKey="mood" value={mockStats.mood} showLabel />
          <StatBar statKey="money" value={mockStats.money} showLabel />
          <StatBar statKey="selfEsteem" value={mockStats.selfEsteem} showLabel />
        </div>
      </Card>

      <div style={{ marginTop: '1rem', display: 'grid', gap: '1rem' }}>
        <Card variant="default" padding="medium">
          <h3>Default Card</h3>
          <p>Это обычная карточка без тени.</p>
        </Card>

        <Card variant="elevated" padding="medium">
          <h3>Elevated Card</h3>
          <p>Карточка с тенью (hover для увеличения).</p>
        </Card>

        <Card 
          variant="outlined" 
          padding="medium"
          onClick={() => alert('Карточка кликабельна!')}
        >
          <h3>Clickable Card</h3>
          <p>Кликни на меня! Увидишь haptic feedback.</p>
        </Card>
      </div>

      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>Loading Spinners</h2>
        <div style={{ display: 'flex', gap: '2rem', justifyContent: 'space-around' }}>
          <LoadingSpinner size="small" />
          <LoadingSpinner size="medium" text="Загрузка..." />
          <LoadingSpinner size="large" />
        </div>
      </Card>

      <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
        <h2>Error Message</h2>
        <Button 
          variant="accent" 
          onClick={() => setShowError(!showError)}
          fullWidth
        >
          {showError ? 'Скрыть' : 'Показать'} ошибку
        </Button>
        {showError && (
          <ErrorMessage 
            message="Не удалось загрузить данные. Проверьте подключение к интернету."
            onRetry={() => {
              setShowError(false);
              alert('Повторная попытка...');
            }}
          />
        )}
      </Card>

      {/* Кнопка выключения в конце */}
      <Card variant="elevated" padding="large" style={{ marginTop: '1rem', marginBottom: '2rem' }}>
        <h2 style={{ color: '#e74c3c' }}>⚙️ Управление демо</h2>
        <p style={{ marginBottom: '1rem' }}>Завершить работу демо-приложения:</p>
        <Button 
          variant="outline" 
          onClick={handleShutdown}
          disabled={isShuttingDown}
          fullWidth
          style={{ 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            gap: '0.5rem',
            color: '#e74c3c',
            borderColor: '#e74c3c'
          }}
        >
          <Power size={20} />
          {isShuttingDown ? 'Выключение...' : 'Выключить приложение'}
        </Button>
        <p style={{ marginTop: '0.5rem', fontSize: '0.9rem', color: '#666', textAlign: 'center' }}>
          Также можно использовать Ctrl+C в консоли
        </p>
      </Card>
    </div>
  );
}

export default ComponentTest;
