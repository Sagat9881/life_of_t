import { useEventStream } from '../../hooks/useEventStream';

export function EventStreamExample() {
  const { status, isConnected, connect } = useEventStream({
    sessionId: 'demo',
    autoConnect: false,
  });

  return (
    <div className="event-stream">
      <h3>Событийный поток</h3>
      <p>Статус: {status}</p>
      <p>Подключено: {isConnected ? 'Да' : 'Нет'}</p>
      <button onClick={connect}>Подключиться</button>
    </div>
  );
}
