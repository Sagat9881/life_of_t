import { useEventStream } from '../../hooks/useEventStream';

export function EventStreamExample() {
  const events = useEventStream();

  return (
    <div className="event-stream">
      <h3>События</h3>
      {events.length === 0 ? (
        <p>Нет событий</p>
      ) : (
        <ul>
          {events.map((event, idx) => (
            <li key={idx}>
              <strong>{event.type}</strong>: {JSON.stringify(event.data)}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
