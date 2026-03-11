package ru.lifegame.backend.infrastructure.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.lifegame.backend.domain.event.domain.DomainEvent;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Listens to all DomainEvents published by Spring's ApplicationEventPublisher
 * and broadcasts them as generic JSON to connected SSE clients.
 * 
 * Uses reflection to extract all record fields automatically -
 * NO manual DTO mapping needed when adding new event types.
 */
@Component
public class DomainEventListener {
    private static final Logger log = LoggerFactory.getLogger(DomainEventListener.class);
    private final EventBroadcaster eventBroadcaster;
    private final ObjectMapper objectMapper;

    public DomainEventListener(EventBroadcaster eventBroadcaster, ObjectMapper objectMapper) {
        this.eventBroadcaster = eventBroadcaster;
        this.objectMapper = objectMapper;
    }

    /**
     * Listens to ALL DomainEvent implementations.
     * Automatically serializes event to JSON using reflection on record components.
     */
    @Async("eventExecutor")
    @EventListener
    public void onDomainEvent(DomainEvent event) {
        try {
            String sessionId = event.sessionId();
            String eventType = event.eventType();
            
            // Convert event to generic Map using reflection
            Map<String, Object> payload = serializeEventToMap(event);
            
            // Add standard fields
            payload.put("eventType", eventType);
            payload.put("timestamp", event.timestamp().toEpochMilli());
            payload.put("sessionId", sessionId);
            
            log.debug("Broadcasting domain event: {} for session: {}", eventType, sessionId);
            eventBroadcaster.broadcast(sessionId, eventType, payload);
            
        } catch (Exception e) {
            log.error("Failed to broadcast domain event: {}", event.getClass().getSimpleName(), e);
        }
    }

    /**
     * Serialize a DomainEvent record to Map using reflection.
     * Works for any record implementing DomainEvent - no manual mapping needed.
     */
    private Map<String, Object> serializeEventToMap(DomainEvent event) {
        Map<String, Object> map = new HashMap<>();
        
        try {
            // Get all record components (fields)
            Class<?> clazz = event.getClass();
            
            // For Java records, all fields have corresponding accessor methods
            for (Method method : clazz.getMethods()) {
                // Skip methods from Object, DomainEvent interface, etc.
                if (method.getDeclaringClass() == Object.class) continue;
                if (method.getName().equals("eventType") || 
                    method.getName().equals("timestamp") || 
                    method.getName().equals("sessionId")) continue;
                
                // Record accessors have no parameters
                if (method.getParameterCount() == 0 && 
                    !method.getReturnType().equals(Void.TYPE)) {
                    
                    String fieldName = method.getName();
                    Object value = method.invoke(event);
                    
                    // Convert complex objects to JSON-serializable format
                    if (value != null && !isPrimitive(value)) {
                        value = objectMapper.convertValue(value, Object.class);
                    }
                    
                    map.put(fieldName, value);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to serialize event using reflection, falling back to toString: {}", 
                     event.getClass().getSimpleName(), e);
            map.put("_rawData", event.toString());
        }
        
        return map;
    }

    private boolean isPrimitive(Object value) {
        return value instanceof String || 
               value instanceof Number || 
               value instanceof Boolean || 
               value.getClass().isPrimitive();
    }
}
