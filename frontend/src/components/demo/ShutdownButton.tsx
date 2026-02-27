import { useState } from 'react';
import { Power } from 'lucide-react';
import styles from './ShutdownButton.module.css';

/**
 * Кнопка выключения демо-приложения
 */
export const ShutdownButton = () => {
  const [isShuttingDown, setIsShuttingDown] = useState(false);

  const handleShutdown = async () => {
    if (!confirm('Выключить демо-приложение?')) {
      return;
    }

    setIsShuttingDown(true);

    try {
      const response = await fetch('/api/demo/shutdown', {
        method: 'POST',
      });

      if (response.ok) {
        // Показываем сообщение
        document.body.innerHTML = `
          <div style="
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            height: 100vh;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            text-align: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
          ">
            <h1 style="font-size: 48px; margin: 0;">✅</h1>
            <h2 style="margin: 16px 0;">Демо выключено</h2>
            <p style="opacity: 0.8;">Спасибо за использование Life of T!</p>
            <p style="opacity: 0.6; margin-top: 32px; font-size: 14px;">Вы можете закрыть это окно</p>
          </div>
        `;
      }
    } catch (error) {
      console.error('Ошибка при выключении:', error);
      alert('Не удалось выключить демо. Используйте Ctrl+C в консоли.');
      setIsShuttingDown(false);
    }
  };

  return (
    <button
      onClick={handleShutdown}
      disabled={isShuttingDown}
      className={styles.shutdownButton}
      title="Выключить демо"
    >
      <Power size={20} />
      <span>{isShuttingDown ? 'Выключение...' : 'Выключить'}</span>
    </button>
  );
};
