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
import { type NavItem } from './components/layout/BottomNav';
import { AppLayout } from './components/layout/AppLayout';
import type { Stats, Player, ActionOption, NPC, PetView, ConflictView, EventView } from './types/game';
import { Power } from 'lucide-react';

function ComponentTest() {
  const [currentNav, setCurrentNav] = useState<NavItem>('home');
  const [isLoading, setIsLoading] = useState(false);
  const [showError, setShowError] = useState(false);
  const [isShuttingDown, setIsShuttingDown] = useState(false);

  const mockStats: Stats = { energy: 75, health: 60, stress: 40, mood: 80, money: 1500, selfEsteem: 70 };

  const mockPlayer: Player = {
    id: 'demo-player', name: 'Таня', stats: mockStats,
    job: { title: 'Дизайнер', satisfaction: 70, burnoutRisk: 30 },
    location: 'home', tags: {}, skills: {}, inventory: [],
  };

  const mockActions: ActionOption[] = [
    { code: 'work_design', label: '💼 Работа (Дизайн)', description: 'Поработать над проектом на Tilda', estimatedTimeCost: 2, isAvailable: true },
    { code: 'rest_tv', label: '📺 Смотреть сериал', description: 'Расслабиться перед телевизором', estimatedTimeCost: 1, isAvailable: true },
    { code: 'social_husband', label: '💑 Время с мужем', description: 'Провести время вместе', estimatedTimeCost: 2, isAvailable: true },
    { code: 'hobby_reading', label: '📚 Почитать книгу', description: 'Погрузиться в роман', estimatedTimeCost: 1, isAvailable: true },
    { code: 'pet_garfield', label: '🐱 Поиграть с Гарфилдом', description: 'Уделить время коту', estimatedTimeCost: 1, isAvailable: true },
    { code: 'sleep', label: '😴 Спать', description: 'Восстановить силы', estimatedTimeCost: 8, isAvailable: false, unavailableReason: 'Ещё не время спать' },
  ];

  const mockNPCs: NPC[] = [
    { id: 'npc-husband', name: 'Александр', type: 'husband', relationship: 85 },
    { id: 'npc-father', name: 'Папа', type: 'father', relationship: 65 },
  ];

  const mockPets: PetView[] = [
    { petId: 'pet-garfield', petCode: 'garfield', name: 'Гарфилд', mood: 90, satiety: 70, attention: 80, health: 95 },
    { petId: 'pet-duke', petCode: 'duke', name: 'Дюк', mood: 75, satiety: 50, attention: 60, health: 85 },
  ];

  const mockConflict: ConflictView = {
    id: 'conflict-1', conflictCode: 'work_life_balance',
    label: 'Муж недоволен тем, что вы слишком много работаете',
    stage: 'ESCALATION', playerCSP: 65, opponentCSP: 40,
    tactics: [
      { code: 'apologize', label: '🙏 Извиниться', description: 'Признать свою ошибку' },
      { code: 'explain', label: '💬 Объяснить', description: 'Спокойно объяснить причины' },
      { code: 'compromise', label: '🤝 Компромисс', description: 'Предложить решение' },
      { code: 'ignore', label: '🚪 Игнорировать', description: 'Сделать вид, что ничего не было' },
    ],
  };

  const mockEvent: EventView = {
    id: 'event-1', label: '🎁 Неожиданный подарок',
    description: 'Вы нашли коробку с котятами у подъезда.',
    options: [
      { code: 'take_home', label: 'Забрать домой', description: '+20 настроение' },
      { code: 'call_shelter', label: 'Позвонить в приют', description: '+10 настроение' },
      { code: 'ask_neighbors', label: 'Спросить соседей', description: '+5 настроение' },
      { code: 'ignore', label: 'Пройти мимо', description: '-10 настроение' },
    ],
  };

  const handleButtonClick = () => { setIsLoading(true); setTimeout(() => setIsLoading(false), 2000); };
  const handleActionExecute = (actionCode: string) => alert(`Выполнено: ${actionCode}`);
  const handleNPCClick = (npcId: string) => alert(`Диалог с: ${npcId}`);
  const handlePetClick = (petId: string) => alert(`Питомец: ${petId}`);
  const handleTacticSelect = (_conflictId: string, tacticCode: string) => alert(`Тактика: ${tacticCode}`);
  const handleChoiceSelect = (_eventId: string, choiceCode: string) => alert(`Выбор: ${choiceCode}`);
  const handleShutdown = async () => {
    if (!confirm('Выключить демо-приложение?')) return;
    setIsShuttingDown(true);
    try { await fetch('/api/shutdown', { method: 'POST' }); alert('Выключается...'); }
    catch (e) { console.error(e); setIsShuttingDown(false); alert('Ошибка выключения'); }
  };

  return (
    <AppLayout currentNav={currentNav} onNavigate={setCurrentNav}>
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        <h1 style={{ fontFamily: 'Comfortaa, sans-serif', color: '#FF6B9D' }}>🎮 Component Test</h1>

        <Card variant="elevated" padding="large">
          <h2>EventChoice</h2>
          <EventChoice event={mockEvent} onSelectChoice={handleChoiceSelect} onCancel={() => alert('Отмена')} />
        </Card>

        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>ConflictResolver</h2>
          <ConflictResolver conflict={mockConflict} onSelectTactic={handleTacticSelect} onCancel={() => alert('Отмена')} />
        </Card>

        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>RelationshipList</h2>
          <RelationshipList npcs={mockNPCs} pets={mockPets} onNPCClick={handleNPCClick} onPetClick={handlePetClick} />
        </Card>

        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>ActionList</h2>
          <ActionList actions={mockActions} onExecuteAction={handleActionExecute} />
        </Card>

        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>PlayerPanel</h2>
          <PlayerPanel player={mockPlayer} />
          <div style={{ marginTop: '1.5rem' }}><h3>Компактная:</h3><PlayerPanel player={mockPlayer} compact /></div>
        </Card>

        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>Buttons</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <Button variant="primary" onClick={handleButtonClick}>Primary</Button>
            <Button variant="secondary" onClick={handleButtonClick}>Secondary</Button>
            <Button variant="accent" onClick={handleButtonClick}>Accent</Button>
            <Button variant="outline" onClick={handleButtonClick}>Outline</Button>
            <Button variant="primary" isLoading={isLoading}>Loading</Button>
            <Button variant="primary" disabled>Disabled</Button>
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
          <Button variant="accent" onClick={() => setShowError(!showError)} fullWidth>
            {showError ? 'Скрыть' : 'Показать'} ошибку
          </Button>
          {showError && <ErrorMessage message="Не удалось загрузить данные." onRetry={() => { setShowError(false); alert('Повтор...'); }} />}
        </Card>

        <Card variant="elevated" padding="large" style={{ marginTop: '1rem', marginBottom: '2rem' }}>
          <h2 style={{ color: '#e74c3c' }}>⚙️ Управление демо</h2>
          <Button variant="outline" onClick={handleShutdown} disabled={isShuttingDown} fullWidth
            style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', color: '#e74c3c', borderColor: '#e74c3c' }}>
            <Power size={20} />{isShuttingDown ? 'Выключение...' : 'Выключить приложение'}
          </Button>
        </Card>
      </div>
    </AppLayout>
  );
}

export default ComponentTest;
