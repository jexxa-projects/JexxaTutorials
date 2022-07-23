package io.jexxa.tutorials.bookstore.domain.common;

import java.util.ArrayList;
import java.util.List;

public class DomainEventPublisher {

    private final List<DomainEventSubscriber<?>> subscribers = new ArrayList<>();
    private static final DomainEventPublisher instance = new DomainEventPublisher();

    public static DomainEventPublisher instance() {
        return instance;
    }

    public <T> void publish(final T aDomainEvent) {
        subscribers
                .stream()
                .filter(element -> element.subscribedToEventType().isAssignableFrom(aDomainEvent.getClass()))
                .forEach(element -> element.handleEvent(aDomainEvent));
    }

    public <T> void subscribe(DomainEventSubscriber<T> subscriber) {
        if (!subscribers.contains(subscriber))
        {
            this.subscribers.add(subscriber);
        }
    }
}
