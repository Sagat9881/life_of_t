import { useState } from 'react';
import { Button } from './components/shared/Button';
import { Card } from './components/shared/Card';
import { StatBar } from './components/shared/StatBar';
import { LoadingSpinner } from './components/shared/LoadingSpinner';
import { ErrorMessage } from './components/shared/ErrorMessage';
import { BottomNav, type NavItem } from './components/layout/BottomNav';
import { AppLayout } from './components/layout/AppLayout';
import type { Stats } from './types/game';

function ComponentTest() {
  const [currentNav, setCurrentNav] = useState<NavItem>('home');
  const [isLoading, setIsLoading] = useState(false);
  const [showError, setShowError] = useState(false);

  const mockStats: Stats = {
    energy: 75,
    health: 60,
    stress: 40,
    mood: 80,
    money: 1500,
    selfEsteem: 70,
  };

  const handleButtonClick = () => {
    setIsLoading(true);
    setTimeout(() => setIsLoading(false), 2000);
  };

  return (
    <AppLayout currentNav={currentNav} onNavigate={setCurrentNav}>
      <div style={{ maxWidth: '600px', margin: '0 auto' }}>
        <h1 style={{ fontFamily: 'Comfortaa, sans-serif', color: '#FF6B9D' }}>
          🎮 Component Test
        </h1>

        {/* Buttons Section */}
        <Card variant="elevated" padding="large">
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

        {/* Stats Section */}
        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>StatBars</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <StatBar statKey="energy" value={mockStats.energy} />
            <StatBar statKey="health" value={mockStats.health} />
            <StatBar statKey="stress" value={mockStats.stress} />
            <StatBar statKey="mood" value={mockStats.mood} />
            <StatBar statKey="money" value={mockStats.money} />
            <StatBar statKey="selfEsteem" value={mockStats.selfEsteem} />
          </div>
        </Card>

        {/* Cards Section */}
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

        {/* Loading Spinner */}
        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>Loading Spinners</h2>
          <div style={{ display: 'flex', gap: '2rem', justifyContent: 'space-around' }}>
            <LoadingSpinner size="small" />
            <LoadingSpinner size="medium" text="Загрузка..." />
            <LoadingSpinner size="large" />
          </div>
        </Card>

        {/* Error Message */}
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

        {/* Navigation Info */}
        <Card variant="elevated" padding="large" style={{ marginTop: '1rem' }}>
          <h2>Bottom Navigation</h2>
          <p>Текущая вкладка: <strong>{currentNav}</strong></p>
          <p>Переключай вкладки внизу экрана и почувствуй haptic feedback!</p>
        </Card>

        {/* Info */}
        <Card variant="outlined" padding="medium" style={{ marginTop: '1rem', marginBottom: '2rem' }}>
          <h3>💡 Информация</h3>
          <ul style={{ paddingLeft: '1.5rem' }}>
            <li>Haptic feedback работает только в Telegram Mini App</li>
            <li>Все кнопки должны откликаться на клики</li>
            <li>Проверь hover эффекты на десктопе</li>
            <li>Цвета: 🌸 Розовый, 🌿 Мятный, ☀️ Жёлтый</li>
          </ul>
        </Card>
      </div>
    </AppLayout>
  );
}

export default ComponentTest;
