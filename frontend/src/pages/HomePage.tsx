import { useState } from 'react';
import { gameApi } from '../api/client';

export const HomePage = () => {
  const [apiStatus, setApiStatus] = useState<string>('Не проверено');
  const [isLoading, setIsLoading] = useState(false);

  const testApi = async () => {
    setIsLoading(true);
    try {
      const result = await gameApi.startSession({ telegramUserId: 'demo-user' });
      setApiStatus(`✅ API работает! Игрок: ${result.player?.name || 'Неизвестно'}`);
      console.log('🎮 API Response:', result);
    } catch (error) {
      setApiStatus(`❌ Ошибка: ${error instanceof Error ? error.message : 'Неизвестная ошибка'}`);
      console.error('❌ API Error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'system-ui' }}>
      <h1 style={{ color: '#FF6B9D', marginBottom: '20px' }}>
        🎮 Life of T - Жизнь Татьяны
      </h1>
      
      <div style={{ 
        background: '#f7f7f7', 
        padding: '15px', 
        borderRadius: '8px',
        marginBottom: '20px'
      }}>
        <h2 style={{ fontSize: '18px', marginBottom: '10px' }}>📊 Статистика (тестовые данные)</h2>
        <div style={{ display: 'grid', gap: '10px' }}>
          <div>⚡ Энергия: <strong>100</strong></div>
          <div>❤️ Здоровье: <strong>100</strong></div>
          <div>😌 Настроение: <strong>80</strong></div>
          <div>💰 Деньги: <strong>1000</strong></div>
        </div>
      </div>

      <div style={{ 
        background: '#fff', 
        border: '2px solid #FF6B9D', 
        padding: '15px', 
        borderRadius: '8px',
        marginBottom: '20px'
      }}>
        <h2 style={{ fontSize: '18px', marginBottom: '10px' }}>🔌 Тест API</h2>
        <p style={{ marginBottom: '10px', color: '#666' }}>
          Статус: <strong>{apiStatus}</strong>
        </p>
        <button 
          onClick={testApi}
          disabled={isLoading}
          style={{
            width: '100%',
            padding: '12px',
            fontSize: '16px',
            fontWeight: 'bold',
            color: '#fff',
            background: isLoading ? '#ccc' : '#FF6B9D',
            border: 'none',
            borderRadius: '8px',
            cursor: isLoading ? 'not-allowed' : 'pointer',
            transition: 'background 0.2s'
          }}
        >
          {isLoading ? '⏳ Загрузка...' : '🚀 Проверить подключение'}
        </button>
      </div>

      <div style={{ 
        background: '#e8f5e9', 
        padding: '15px', 
        borderRadius: '8px',
        fontSize: '14px'
      }}>
        <h3 style={{ fontSize: '16px', marginBottom: '10px' }}>✅ Что работает:</h3>
        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
          <li>✓ Frontend загружен</li>
          <li>✓ React работает</li>
          <li>✓ Стили применяются</li>
          <li>✓ Backend отвечает на :3000</li>
        </ul>
      </div>

      <div style={{ marginTop: '20px', fontSize: '12px', color: '#999', textAlign: 'center' }}>
        🛠️ Demo Version | Build: {new Date().toLocaleString('ru-RU')}
      </div>
    </div>
  );
};
