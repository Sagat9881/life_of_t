import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Check, X, Loader2 } from 'lucide-react';
import { useTelegram } from '../../hooks/useTelegram';
import type { GameAction } from '../../types/game';
import '../../styles/components/ActionCard.css';

interface ActionCardProps {
  action: GameAction;
  onExecute?: (actionCode: string) => void;
}

export function ActionCard({ action, onExecute }: ActionCardProps) {
  const { hapticFeedback } = useTelegram();
  const [isProcessing, setIsProcessing] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [isError, setIsError] = useState(false);

  const handleExecute = async () => {
    if (!onExecute || isProcessing) return;

    // Тактильный отклик
    hapticFeedback?.impactOccurred('medium');

    // Начало обработки
    setIsProcessing(true);
    setIsSuccess(false);
    setIsError(false);

    try {
      await onExecute(action.code);
      
      // Успех
      setIsSuccess(true);
      hapticFeedback?.impactOccurred('light');
      
      // Сброс через 1.5с
      setTimeout(() => {
        setIsSuccess(false);
        setIsProcessing(false);
      }, 1500);
    } catch (error) {
      // Ошибка
      setIsError(true);
      hapticFeedback?.impactOccurred('heavy');
      
      // Сброс через 2с
      setTimeout(() => {
        setIsError(false);
        setIsProcessing(false);
      }, 2000);
    }
  };

  const energyCost = action.energyCost ?? 0;
  const isDisabled = energyCost > 100 || isProcessing;

  return (
    <motion.div
      className={`action-card ${
        isProcessing ? 'action-card--processing' : ''
      } ${
        isSuccess ? 'action-card--success' : ''
      } ${
        isError ? 'action-card--error' : ''
      } ${
        isDisabled ? 'action-card--disabled' : ''
      }`}
      whileHover={!isDisabled ? { scale: 1.02, y: -4 } : {}}
      whileTap={!isDisabled ? { scale: 0.98 } : {}}
      onClick={handleExecute}
      layout
    >
      {/* Анимация обработки */}
      <AnimatePresence>
        {isProcessing && (
          <motion.div
            className="action-card__processing-overlay"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div
              className="action-card__processing-spinner"
              animate={{ rotate: 360 }}
              transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
            >
              <Loader2 size={32} />
            </motion.div>
            <p className="action-card__processing-text">Обработка...</p>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Иконка успеха */}
      <AnimatePresence>
        {isSuccess && (
          <motion.div
            className="action-card__success-icon"
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            exit={{ scale: 0, opacity: 0 }}
            transition={{ type: 'spring', stiffness: 300, damping: 20 }}
          >
            <Check size={48} strokeWidth={3} />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Иконка ошибки */}
      <AnimatePresence>
        {isError && (
          <motion.div
            className="action-card__error-icon"
            initial={{ scale: 0, opacity: 0 }}
            animate={{ 
              scale: [0, 1.2, 1],
              opacity: 1,
              rotate: [0, -10, 10, -10, 0]
            }}
            exit={{ scale: 0, opacity: 0 }}
            transition={{ duration: 0.5 }}
          >
            <X size={48} strokeWidth={3} />
          </motion.div>
        )}
      </AnimatePresence>

      {/* Основной контент */}
      <div className="action-card__header">
        <h3 className="action-card__title">{action.name}</h3>
      </div>

      <p className="action-card__description">{action.description}</p>

      <div className="action-card__costs">
        {energyCost > 0 && (
          <span className="action-card__cost action-card__cost--energy">
            ⚡ {energyCost}
          </span>
        )}
        {action.timeCost > 0 && (
          <span className="action-card__cost action-card__cost--time">
            ⏱️ {action.timeCost}ч
          </span>
        )}
      </div>

      {action.category && (
        <div className="action-card__category">
          <span className="action-card__category-badge">
            {action.category}
          </span>
        </div>
      )}

      {isDisabled && (
        <div className="action-card__disabled-overlay">
          <p>Недостаточно энергии</p>
        </div>
      )}
    </motion.div>
  );
}
