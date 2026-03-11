package ru.lifegame.backend.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.lifegame.backend.application.port.out.EventPublisher;
import ru.lifegame.backend.domain.event.domain.DomainEvent;

@Component
public class SpringEventPublisher implements EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(SpringEventPublisher.class);
    private final ApplicationEventPublisher springPublisher;

    public SpringEventPublisher(ApplicationEventPublisher springPublisher) {
        this.springPublisher = springPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event to Spring context: type={}, sessionId={}",
                event.getClass().getSimpleName(), event.sessionId());
        springPublisher.publishEvent(event);
    }
}
